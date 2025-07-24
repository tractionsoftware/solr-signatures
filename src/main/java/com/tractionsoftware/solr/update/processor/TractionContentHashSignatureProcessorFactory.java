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
import org.apache.solr.common.util.NamedList;

public class TractionContentHashSignatureProcessorFactory extends TractionCustomSignatureUpdateProcessorFactory {

    public static final String FIELD_NAME_CONTENT_HASH = "content_hash";

    public static final TractionContentHashSignatureProcessorFactory createInstance(NamedList<?> args) {

        NamedList<Object> useArgs = TractionSignatureUpdateProcessorFactory.createBaseArgs(args, NoOpSignature.class);
        useArgs.add("fields", TractionContentHashSignatureProcessorFactory.FIELD_NAME_CONTENT_HASH);

        TractionContentHashSignatureProcessorFactory factory = new TractionContentHashSignatureProcessorFactory();
        factory.init(args);
        return factory;

    }

    @Override
    protected final String getSignature(SolrInputDocument doc) {
        return getRequiredSingleFieldValue(doc, FIELD_NAME_CONTENT_HASH);
    }

}
