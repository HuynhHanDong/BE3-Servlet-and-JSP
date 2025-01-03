/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import entities.Account;
import dao.AccountDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author vothimaihoa
 */
public class AuthController extends HttpServlet {
    private final String LIST = "Product";
    private final String LOGIN_VIEW = "view/account/login.jsp";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.getByUsernamePassword(username, password);
        if (account != null) {
            resp.sendRedirect(LIST);
        }
        else {
            req.setAttribute("error", "Wrong username or password"); 
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
        }
    }
}
