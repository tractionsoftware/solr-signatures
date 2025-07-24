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

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.update.processor.Lookup3Signature;
import org.apache.solr.update.processor.SignatureUpdateProcessorFactory;

public class TractionOtherSignatureProcessorFactory extends SignatureUpdateProcessorFactory {

    public static final TractionOtherSignatureProcessorFactory createInstance(NamedList<?> args) {

        NamedList<Object> useArgs = TractionSignatureUpdateProcessorFactory.createBaseArgs(args, null);
        SolrParams params = args.toSolrParams();

        useArgs.add(
            "signatureClass",
            params.get("otherSignatureClass", Lookup3Signature.class.getName())
        );

        // By default, use all fields, but allow configuration to specify a list.
        Object fields = params.get("otherSignatureFields");
        if (fields != null) {
            useArgs.add("fields", fields);
        }

        TractionOtherSignatureProcessorFactory factory = new TractionOtherSignatureProcessorFactory();
        factory.init(useArgs);
        return factory;

    }

}
