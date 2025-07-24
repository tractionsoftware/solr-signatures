# solr-signatures
SignatureUpdateProcessorFactory implementations that apply signatures specific for Traction TeamPage's Solr search integration.

The pom.xml file targets Java 21, but can probably work with earlier versions of Java with few or no changes.

These classes are specific and quite tightly bound to various aspects of TeamPage's Solr integration, but may be of use as examples to someone who needs to create a similar customization.

- NoOpSignature: A Signature implementation that produces an empty byte array. It is mostly referenced as a place-holder in certain sets of parameters.
- TractionSignatureUpdateProcessorFactory: Determines what type of signature strategy to apply based on certain document properties and invariants needed for TeamPage's search index, and selects the correct SignatureUpdateProcessorFactory on that basis.
- TractionCustomSignatureUpdateProcessorFactory: A base class some of the custom SignatureUpdateProcessorFactory classes.
- TractionOtherSignatureProcessorFactory: The configurable "other" option that uses the configured Signature class and fields in the "signatureFields" configuration parameter. The default behavior is to use the built-in Lookup3Signature, and to use all document fields.
- TractionTextSignatureProcessorFactory: Uses the built-in TextProfileSignature class to compute a signature from the document's "title" and "text" fields.
- TractionUniqueIdSignatureProcessorFactory: Copies a unique identifier from another field to the signature field. This is appropriate for documents that should never be considered duplicates.
- TractionContentHashSignatureProcessorFactory: Copies a value already set on the document's "content_hash" field to the signature field. This is appropriate for signatures that the indexer prefers to compute in advance, possibly because only the document's metadata is being indexed (and therefore there is no document data for an ordinary signature computation to operate on).
