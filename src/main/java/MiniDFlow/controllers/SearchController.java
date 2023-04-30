package MiniDFlow.controllers;

import MiniDFlow.security.JdbcAuthorService;
import MiniDFlow.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class SearchController {
    @Autowired
    DocumentService documentService;
    @Autowired
    JdbcAuthorService userDetailsManager;
    @GetMapping("/doc/search")
    public String search(@RequestParam(value ="q",required = true) String q,
                         @RequestParam(value = "l",defaultValue = "20",required = false) int limit,
                         @RequestParam(value = "t",required = false,defaultValue = "false") boolean searchInTexts,
                         Model model,
                         Principal principal){
        if(!q.isEmpty()) {
            model.addAttribute("user", userDetailsManager.loadUserByPrincipal(principal));
            model.addAttribute("documentViewList",documentService.searchDocs(q,limit,searchInTexts));
            return "main";
        }
        return "redirect:/doc/main";
    }

}
