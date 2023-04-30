package MiniDFlow.entity;

import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Document")
public class Document implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documentId")
    private Integer id;

    @Column(name = "documentName")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author", nullable = false)
    private Author author;

    @Formula(value = "(select max(dv.version) from DocumentVersion dv where dv.documentId = documentId)")
    protected int lastVersion;

    @Formula(value = "(select (rg.dateExtern is null) from RegisterCard rg where rg.documentId = documentId)")
    protected boolean isExist;
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "documentId")
    private RegisterCard registerCard;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "documentId")
    private Set<DocumentVersion> documentVersion;

    public Document() {
    }

    public Document(String name, Author author) {
        this.name = name;
        this.author = author;
    }

    public Document(Integer id, String name, Author author) {
        this.id = id;
        this.name = name;
        this.author = author;
    }

    public int getLastVersion() {
        return lastVersion;
    }

    public boolean isExist() {
        return isExist;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document document)) return false;
        return id.equals(document.id) && name.equals(document.name) && author.equals(document.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, author);
    }

    public void setLastVersion(int lastVersion) {
        this.lastVersion = lastVersion;
    }

    public void setExist(boolean exist) {
        isExist = exist;
    }

    public RegisterCard getRegisterCard() {
        return registerCard;
    }

    public void setRegisterCard(RegisterCard registerCard) {
        this.registerCard = registerCard;
    }

    public Set<DocumentVersion> getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(Set<DocumentVersion> documentVersion) {
        this.documentVersion = documentVersion;
    }
}
