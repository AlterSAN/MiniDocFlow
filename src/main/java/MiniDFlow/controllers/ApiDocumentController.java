package MiniDFlow.controllers;

import MiniDFlow.POJO.DocumentPOJO;
import MiniDFlow.POJO.DocumentUpdatePOJO;
import MiniDFlow.entity.DocumentVersion;
import MiniDFlow.entity.projection.DocumentView;
import MiniDFlow.security.JdbcAuthorService;
import MiniDFlow.service.AuthorService;
import MiniDFlow.service.DocumentService;
import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;


@Controller
@RequestMapping("/doc")
public class ApiDocumentController {
    @Autowired
    private DocumentService documentService;
    @Autowired
    private AuthorService authorService;
    @Autowired
    JdbcAuthorService userDetailsManager;
    @PostMapping("/new")
    public String newDocumentSave(
            @Valid @ModelAttribute("documentPOJO") DocumentPOJO documentPOJO,
            BindingResult result,
            Authentication authentication
            ) throws IOException {
        if(result.hasErrors()){
            return "fileUploadView";
        }
        documentService.createNewDocument(documentPOJO,authorService.findByUsername(authentication.getName()));
        return "redirect:/doc/main";
    }

    @GetMapping("/new")
    public String newDocumentView(Model model, Principal principal){
        model.addAttribute("username", principal.getName());
        model.addAttribute("documentPOJO",new DocumentPOJO());
        return "fileUploadView";
    }


    @PostMapping("/update")
    public String updateDocumentById(@Valid @ModelAttribute("documentUpdatePOJO") DocumentUpdatePOJO documentUpdatePOJO, Authentication authentication) throws IOException {
        documentService.updateDocumentById(documentUpdatePOJO,authorService.findByUsername(authentication.getName()));
        return "redirect:/doc/main";
    }
    @PostMapping("{id}/update")
    public String updateDocumentByParamId(@PathVariable("id")int id,
                                          @ModelAttribute("documentUpdatePOJO") DocumentUpdatePOJO documentUpdatePOJO,
                                          Authentication authentication
                                         ) throws IOException {
        documentUpdatePOJO.setDocumentId(id);
        documentService.updateDocumentById(documentUpdatePOJO,authorService.findByUsername(authentication.getName()));
        return "redirect:/doc/main";
    }

    @GetMapping("{id}/update")
    public String getUpdateViewById(@PathVariable("id") int id,Model model,Principal principal){
        DocumentView documentView = documentService.getDocById(id,-1);
        model.addAttribute("documentPOJO",new DocumentUpdatePOJO(documentView.getDocumentId()));
        model.addAttribute("docName",documentView.getDocumentName());
        model.addAttribute("username",principal.getName());
        return "updateView";
    }
    @GetMapping("/update")
    public String getUpdateView(Model model, Principal principal){
        model.addAttribute("username",principal.getName());
        model.addAttribute(new DocumentUpdatePOJO());
        return "updateByIdView";
    }

    @GetMapping("/{id}")
    public String getFileById(Model model,
                              @PathVariable("id") int id,
                              Principal principal){
        model.addAttribute("doc",documentService.getDocById(id,-1));
        model.addAttribute("username", principal.getName());
        return "document";
    }
    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") int id,
                         @RequestParam("dEx") String documentExternNumber){
        documentService.deleteDocumentById(id,documentExternNumber);
        return "redirect:/doc/main";
    }

    @GetMapping("/{id}/delete")
    public String getDeleteView(@PathVariable("id") int id, Model model, Principal principal){
        model.addAttribute("doc",documentService.getDocById(id,-1));
        model.addAttribute("username",principal.getName());
        return "deleteView";
    }

    @PostMapping("/{id}")
    public String download(@PathVariable int id,
                           @RequestParam(required = false,defaultValue = "-1",name = "ver") int ver,
                           HttpServletResponse response){
        DocumentVersion docVer = documentService.getFile(id,ver);
        response.setContentType("application/" +FilenameUtils.getExtension(docVer.getDocumentId().getName()));
        response.setHeader("Content-Disposition", "filename=\""+ docVer.getDocumentId().getName()+"\"");
        response.setContentLength(docVer.getContent().length);
        try{
            InputStream inputStream = new ByteArrayInputStream(docVer.getContent());
            IOUtils.copy(inputStream,response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/doc/main";
    }

}
