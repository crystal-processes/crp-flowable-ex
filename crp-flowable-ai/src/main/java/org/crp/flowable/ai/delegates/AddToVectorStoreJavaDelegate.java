package org.crp.flowable.ai.delegates;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

public class AddToVectorStoreJavaDelegate  implements JavaDelegate {
    private  static final Logger LOG = LoggerFactory.getLogger(AddToVectorStoreJavaDelegate.class);

    protected Expression vectorStore;
    protected Expression documentUrl;

    @Override
    public void execute(DelegateExecution execution) {
        LOG.debug("Adding documents to vector store.");
        getVectorStore(execution).add(
                        getDocuments(execution)
                );
        LOG.debug("Documents added to vector store.");
    }

    private List<Document> getDocuments(DelegateExecution execution) {
        return new TikaDocumentReader(getDocumentUrl(execution)).get();
    }

    private String getDocumentUrl(DelegateExecution execution) {
        return ExpressionsHelper.getMandatoryValue("documentUrl", documentUrl, execution, String.class);
    }

    private VectorStore getVectorStore(DelegateExecution execution) {
        return ExpressionsHelper.getMandatoryValue("vectorStore", vectorStore, execution, VectorStore.class);
    }
}
