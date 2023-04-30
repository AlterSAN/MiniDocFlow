package MiniDFlow.service;

import MiniDFlow.POJO.DocumentPOJO;
import MiniDFlow.POJO.DocumentUpdatePOJO;
import MiniDFlow.POJO.Page;
import MiniDFlow.entity.Author;
import MiniDFlow.entity.Document;
import MiniDFlow.entity.DocumentVersion;
import MiniDFlow.entity.RegisterCard;
import MiniDFlow.entity.projection.DocumentView;
import MiniDFlow.repository.DocumentRepository;
import MiniDFlow.repository.DocumentVersionRepository;
import MiniDFlow.repository.RegisterCardRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private RegisterCardRepository registerCardRepository;
    @Autowired
    private DocumentVersionRepository documentVersionRepository;

    @Transactional
    public void createNewDocument(DocumentPOJO documentPOJO, Author author) throws IOException {
        Document document;
        String origDocName = documentPOJO.getFile().getOriginalFilename();
        String ext = FilenameUtils.getExtension(origDocName);
        String docName = documentPOJO.getName() + (ext!=null&&!ext.isBlank()?"."+ext:"");
        documentRepository.create(document = new Document(docName, author));
        registerCardRepository.create(new RegisterCard(document, documentPOJO.getDocumentIntroNumber(), Instant.now()));
        documentVersionRepository.create(new DocumentVersion(document, author, documentPOJO.getFile().getBytes()));
    }

    @Transactional
    public void updateDocumentById(DocumentUpdatePOJO documentUpdatePOJO, Author author) throws IOException {
        Document document = documentRepository.getById(documentUpdatePOJO.getDocumentId());
        if (document.isExist()) {
            documentVersionRepository.update(new DocumentVersion(
                    document,
                    author,
                    documentUpdatePOJO.getFile().getBytes(),
                    document.getLastVersion() + 1)
            );
        } else {
            throw new RuntimeException("Document deleted");
        }
    }

    /**
     * if ver < 1: returns last version
     * @param id document id
     * @param ver version of document
     * @return DocumentView with version
     */
    @Transactional
    public DocumentView getDocById(int id, int ver){
        Document doc = documentRepository.getById(id);
        DocumentVersion docVer = documentVersionRepository.getVersionByDocument(doc,ver<1?doc.getLastVersion():ver);
        return new DocumentView(
                doc.getId(),
                doc.getName(),
                docVer.getVersionAuthor().getUsername(),
                docVer.getVersion(),
                doc.isExist(),
                doc.getRegisterCard().getDateIntro(),
                doc.getRegisterCard().getDocumentIntroNumber()
        );
    }

    @Transactional
    public void deleteDocumentById(int id, String documentExternNumber) {
        registerCardRepository.update(documentRepository.getById(id), documentExternNumber);
    }

    @Transactional
    public DocumentVersion getFile(int id,int ver){
        Document document = documentRepository.getById(id);
        return documentVersionRepository.getVersionByDocument(document,ver>=1?ver:document.getLastVersion());
    }
    @Transactional
    public List<DocumentView> getAllDocViewsByAuthor(Author author) {
        return documentRepository.getAllDocViewsByAuthor(author);
    }
    @Transactional
    public List<DocumentView> searchDocs(String query, int limit,boolean searchInTexts) {
        return documentRepository.searchByDocumentName(query,limit);
    }
    @Transactional
    public Page<DocumentView> getPage(int p, int limit){
        Page<DocumentView> page = new Page<>();
        page.setMaxResults((int)documentRepository.getCount());
        page.setLastPage( (page.getMaxResults()%limit==0)?(page.getMaxResults()/limit):((int)Math.ceil((double) page.getMaxResults()/limit)));
        page.setCurPage(p>page.getLastPage()?(page.getLastPage()):(Math.max(p, 1)));
        page.setLimitPage(Math.max(limit, 3));
        page.setContent(documentRepository.getPage((page.getCurPage()-1)*page.getLimitPage(),page.getLimitPage()));
        return page;
    }
}
