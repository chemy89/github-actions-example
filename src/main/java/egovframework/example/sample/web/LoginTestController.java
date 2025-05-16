package egovframework.example.sample.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.egovframe.boot.security.userdetails.util.EgovUserDetailsHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class LoginTestController {

    @RequestMapping("/accessDenied2")
    public String accessDenied() {
        return "Access Denied!";
    }

    @RequestMapping("/login2")
    public String Login(HttpServletRequest request, HttpServletResponse response, String S) throws IOException {
        // Obtain the user's credentials from the login form
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Create an Authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        ApplicationContext act = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
        // Obtain the AuthenticationManager from the Spring context
        AuthenticationManager authenticationManager = act.getBean("authenticationManager", AuthenticationManager.class);

        // Try to authenticate the user
        try {
            Authentication authenticated = authenticationManager.authenticate(authentication);

            // Set the authenticated user in the security context
            SecurityContextHolder.getContext().setAuthentication(authenticated);

            return "Login Successful!!!";
        } catch (AuthenticationException e) {
            return "Login Failure!!!";
        }
    }

    @RequestMapping("/logout2")
    public String Logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "Logout Successful!!!";
    }

    @RequestMapping("/loginFailure2")
    public String LoginFailure() {
        return "login Failure!";
    }

    @RequestMapping("/consurentExpired2")
    public String consurentExpired() {
        return "consurent Expired!";
    }

    @RequestMapping("/defaultTarget2")
    public String defaultTarget() {
        return "default Target!";
    }

    @RequestMapping("/csrfAccessDenied2")
    public String csrfAccessDenied() {
        return "csrf Access Denied!";
    }

    @RequestMapping("/auth2")
    public void test() {
        Object users = EgovUserDetailsHelper.getAuthenticatedUser();
        log.debug("##### authen users >>> {}", users);

        boolean state = EgovUserDetailsHelper.isAuthenticated();
        log.debug("##### authen state >>> {}", state);

        List<String> list = EgovUserDetailsHelper.getAuthorities();
        log.debug("##### authen list >>> {}", list);

        String accessUser = (String) RequestContextHolder.getRequestAttributes().getAttribute("accessUser", RequestAttributes.SCOPE_SESSION);
        log.debug("##### authen accessUser >>> {}", accessUser);
    }

}
