/*
 *
 *    Copyright 1996-2025 Traction Software, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.

package com.tractionsoftware.solr.update.processor;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.SignatureUpdateProcessorFactory;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public final class TractionSignatureUpdateProcessorFactory extends SignatureUpdateProcessorFactory {

    static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final List<String> POSSIBLE_ID_FIELD_NAMES = List.of(
        "id", "docid"
    );

    private static enum Type {

        /**
         * Applies to documents that have an "id_unique" field, such as TeamPage entries identified by their
         * FQID/Traction ID plus a published/draft/hidden identifier.
         */
        UNIQUE,

        /**
         * Documents that do not have an "id_unique" field, but do have a "text" field representing text extracted by
         * the extraction phase. For these documents, the fuzzy text hash should be used.
         */
        TEXT,

        /**
         * Documents that do not have "id_unique" or "text" fields, but which do have a "content_hash" field, which can
         * be directly used as a de-duplication signature.
         */
        HASHED,

        /**
         * Other documents, for which a signature will be calculated based on either a set of specified fields. By
         * default, this will be the Lookup3Signature using all fields.
         */
        OTHER;

        public static final Type get(SolrInputDocument doc) {
            if (doc.getFieldNames().contains(TractionUniqueIdSignatureProcessorFactory.FIELD_NAME_UNIQUE_ID)) {
                return UNIQUE;
            }
            if (hasText(doc)) {
                return TEXT;
            }
            if (doc.getField(TractionContentHashSignatureProcessorFactory.FIELD_NAME_CONTENT_HASH) != null) {
                return HASHED;
            }
            return OTHER;
        }

        private static final boolean hasText(SolrInputDocument doc) {
            SolrInputField field = doc.getField("text");
            if (field == null) {
                return false;
            }
            Object value = field.getValue();
            if (value == null) {
                return false;
            }
            if (value instanceof Collection<?> c) {
                for (Object v : c) {
                    if (v != null && StrUtils.isNotBlank(String.valueOf(v))) {
                        return true;
                    }
                }
                return false;
            }
            return StrUtils.isNotBlank(String.valueOf(value));
        }

    }

    public static final NamedList<Object> createBaseArgs(NamedList<?> args, Class<?> signatureClass) {

        SolrParams params = args.toSolrParams();

        NamedList<Object> baseArgs = new NamedList<>();
        baseArgs.add("enabled", params.getBool("enabled", true));
        baseArgs.add("overwriteDupes", "false");
        baseArgs.add("signatureField", params.get("signatureField", "__signature"));
        if (signatureClass != null) {
            baseArgs.add("signatureClass", signatureClass.getName());
        }
        return baseArgs;

    }

    private final void set(Type type, SignatureUpdateProcessorFactory factory) {
        type2factory.put(type, factory);
        log.atInfo().log(
            () ->
                "set up " + type + " document signature class (" + factory.getSignatureClass() + ") with fields " +
                fieldNamesSpec(factory.getSigFields())
        );
    }

    private static final String fieldNamesSpec(Collection<String> fieldNames) {
        if (fieldNames == null || fieldNames.isEmpty()) {
            return "[all]";
        }
        return String.join(",", fieldNames);
    }

    private static final String docDebugString(SolrInputDocument doc) {
        for (String fieldName : POSSIBLE_ID_FIELD_NAMES) {
            Object docId = doc.getFieldValue(fieldName);
            if (docId != null) {
                return docId.toString();
            }
        }
        return doc.toString();
    }

    private final class TractionSignatureUpdateProcessor extends UpdateRequestProcessor {

        private final SolrQueryRequest req;

        private final SolrQueryResponse rsp;

        public TractionSignatureUpdateProcessor(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
            super(next);
            this.req = req;
            this.rsp = rsp;
        }

        @Override
        public final void processAdd(AddUpdateCommand cmd) throws IOException {
            try (UpdateRequestProcessor delegate = delegate(cmd)) {
                delegate.processAdd(cmd);
            }
        }

        private final UpdateRequestProcessor delegate(AddUpdateCommand cmd) {
            SignatureUpdateProcessorFactory sig = getSignatureProcessorFactory(Type.get(cmd.getSolrInputDocument()));
            log.atDebug().log(
                () ->
                    "using " +
                    sig.getSignatureClass() +
                    " for signature of " +
                    docDebugString(cmd.getSolrInputDocument())
            );
            return sig.getInstance(req, rsp, next);
        }

    }

    private final EnumMap<Type,SignatureUpdateProcessorFactory> type2factory;

    public TractionSignatureUpdateProcessorFactory() {
        type2factory = new EnumMap<>(Type.class);
    }

    @Override
    public final void init(NamedList<?> args) {

        super.init(createBaseArgs(args, NoOpSignature.class));

        set(Type.UNIQUE, TractionUniqueIdSignatureProcessorFactory.createInstance(args));
        set(Type.TEXT, TractionTextSignatureProcessorFactory.createInstance(args));
        set(Type.HASHED, TractionContentHashSignatureProcessorFactory.createInstance(args));
        set(Type.OTHER, TractionOtherSignatureProcessorFactory.createInstance(args));

    }

    @Override
    public final UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new TractionSignatureUpdateProcessor(req, rsp, next);
    }

    private final SignatureUpdateProcessorFactory getSignatureProcessorFactory(Type type) {
        return type2factory.get(type);
    }

}
