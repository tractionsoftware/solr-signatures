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

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.SignatureUpdateProcessorFactory;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import java.io.IOException;
import java.util.Collection;

public abstract class TractionCustomSignatureUpdateProcessorFactory extends SignatureUpdateProcessorFactory {

    protected final class TractionCustomUpdateRequestProcessor extends UpdateRequestProcessor {

        public TractionCustomUpdateRequestProcessor(UpdateRequestProcessor next) {
            super(next);
        }

        @Override
        public final void processAdd(AddUpdateCommand cmd) throws IOException {
            if (isEnabled()) {
                TractionCustomSignatureUpdateProcessorFactory.this.setSignature(cmd.getSolrInputDocument());
            }
            super.processAdd(cmd);
        }

    }

    @Override
    public final UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new TractionCustomUpdateRequestProcessor(next);
    }

    private final void setSignature(SolrInputDocument doc) {
        try {
            setSignatureImpl(doc);
        }
        catch (RuntimeException e) {
            TractionSignatureUpdateProcessorFactory.log.error("Failed to set signature for ", e);
        }
    }

    private final void setSignatureImpl(SolrInputDocument doc) {
        String signatureField = getSignatureField();
        String signature = getSignature(doc);
        doc.addField(signatureField, signature);
        TractionSignatureUpdateProcessorFactory.log.atDebug().log(
            () -> getClass().getName() + " set " + signatureField + "=" + signature
        );
    }

    protected abstract String getSignature(SolrInputDocument doc);

    protected final String getRequiredSingleFieldValue(SolrInputDocument doc, String fieldName) {

        SolrInputField field = doc.getField(fieldName);
        if (field == null) {
            throw new SolrException(
                SolrException.ErrorCode.BAD_REQUEST,
                getClass().getName() + " requires " + fieldName + " field"
            );
        }

        Object value = field.getValue();
        if (value == null) {
            throw new SolrException(
                SolrException.ErrorCode.BAD_REQUEST,
                getClass().getName() + " requires " + fieldName + " field"
            );
        }

        if (value instanceof Collection<?>) {
            throw new SolrException(
                SolrException.ErrorCode.BAD_REQUEST,
                getClass().getName() + " requires single-valued " + fieldName + " field"
            );
        }

        return String.valueOf(value);

    }

}
