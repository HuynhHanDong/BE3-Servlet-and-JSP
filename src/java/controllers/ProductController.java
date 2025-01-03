/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import dao.CategoryDAO;
import dao.ProductDAO;
import dto.CreateProductDTO;
import dto.SearchProductDTO;
import entities.Category;
import entities.Product;
import exceptions.InvalidDataException;
import exceptions.ValidationException;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author vothimaihoa
 */
public class ProductController extends HttpServlet {

    private final String LIST = "Product";
    private final String LIST_VIEW = "view/product/list.jsp";
    private final String PREPARE_CREATE = "Product?action=prepare-add";
    private final String CREATE_VIEW = "view/product/create.jsp";
    private final String PREPARE_UPDATE = "Product?action=prepare-update";
    private final String UPDATE_VIEW = "view/product/update.jsp";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        String action = request.getParameter("action");
        if (action == null) {
            list(request, response, categoryDAO, productDAO);
        } else {
            switch (action) {
                case "prepare-add":
                    prepareAdd(request, response, categoryDAO);
                    break;
                case "add":
                    add(request, response, categoryDAO, productDAO);
                    break;
                case "prepare-update":
                    prepareUpdate(request, response, categoryDAO, productDAO);
                    break;
                case "update":
                    update(request, response, categoryDAO, productDAO);
                    break;
                case "delete":
                    delete(request, response, productDAO);
                    break;
                default:
                    list(request, response, categoryDAO, productDAO);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private void list(HttpServletRequest request, HttpServletResponse response, CategoryDAO categoryDAO, ProductDAO productDAO)
            throws ServletException, IOException {
        try {
            // get category list for drop down
            List<Category> categories = categoryDAO.list();
            request.setAttribute("categories", categories);

            // get search criterias
            String categoryIdRaw = request.getParameter("category");
            String productName = request.getParameter("productName");
            String minPriceRaw = request.getParameter("minPrice");
            String maxPriceRaw = request.getParameter("maxPrice");

            // validate search fields only when search criterias a string
            Integer categoryId = null;
            Float minPrice = null;
            Float maxPrice = null;
            
            SearchProductDTO searchDTO = new SearchProductDTO(categoryIdRaw, productName, minPriceRaw, maxPriceRaw);
            searchDTO.validate();
                
            if (categoryIdRaw != null && !categoryIdRaw.isEmpty()) {
                categoryId = Integer.parseInt(categoryIdRaw);
            }
            if (minPriceRaw != null && !minPriceRaw.isEmpty()) {
                minPrice = Float.parseFloat(minPriceRaw);
            }
            if (maxPriceRaw != null && !maxPriceRaw.isEmpty()) {
                maxPrice = Float.parseFloat(maxPriceRaw);
            }

            // get search data
            List<Product> list = productDAO.list(productName, categoryId,minPrice, maxPrice);
            if (list != null && !list.isEmpty()) {
                request.setAttribute("products", list);
            } else {
                throw new InvalidDataException("No Products Found!");
            }
            
            // hold search criteria on search bar for next request
            request.setAttribute("productName", productName);
            request.setAttribute("category", categoryIdRaw);
            request.setAttribute("minPrice", minPriceRaw);
            request.setAttribute("maxPrice", maxPriceRaw);

        }
//      catch (ValidationException ex) {
//            request.setAttribute("validationErrors", ex.getErrors());
//      } 
        catch (ValidationException | InvalidDataException ex) {
            request.setAttribute("msg", ex.getMessage());
        } finally {
            request.getRequestDispatcher(LIST_VIEW).forward(request, response);
        }
    }

    private void prepareAdd(HttpServletRequest request, HttpServletResponse response, CategoryDAO categoryDAO) throws ServletException, IOException {
        List<Category> categories = categoryDAO.list();
        request.setAttribute("categories", categories);
        request.getRequestDispatcher(CREATE_VIEW).forward(request, response);
    }

    private void add(HttpServletRequest request, HttpServletResponse response, CategoryDAO categoryDAO, ProductDAO productDAO) throws ServletException, IOException {
        String name = request.getParameter("name");
        String price = request.getParameter("price");
        String productYear = request.getParameter("productYear");
        String image = request.getParameter("image");
        String categoryId = request.getParameter("category");
        
        CreateProductDTO productDTO = new CreateProductDTO(name, price, productYear, image, categoryId);
        try {
            productDTO.validate();

            Category category = categoryDAO.getById(Integer.parseInt(categoryId));
            if (category == null) {
                throw new InvalidDataException("Category not found!");
            }

            Product product = new Product(name, Float.parseFloat(price), Integer.parseInt(productYear), image, category);
            boolean isOk = productDAO.create(product);
            if (!isOk) {
                throw new InvalidDataException("Cannot save product to database!");
            } else {
                response.sendRedirect(LIST);
            }
        } catch (ValidationException | InvalidDataException ex) {
            request.setAttribute("msg", ex.getMessage());
            request.getRequestDispatcher(PREPARE_CREATE).forward(request, response);
        }
    }
    
    private void prepareUpdate(HttpServletRequest request, HttpServletResponse response, CategoryDAO categoryDAO, ProductDAO productDAO) throws ServletException, IOException {
        String idRaw = request.getParameter("id");
        try {
            int id = Integer.parseInt(idRaw);
            Product product = productDAO.getById(id);
            if (product != null) {
            List<Category> categories = categoryDAO.list();
            request.setAttribute("categories", categories);
            request.setAttribute("product", product);
            request.getRequestDispatcher(UPDATE_VIEW).forward(request, response);
            } else {
                    throw new InvalidDataException("Product not found!");
            }
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid product ID!");
        }
    }
    
    private void update(HttpServletRequest request, HttpServletResponse response, CategoryDAO categoryDAO, ProductDAO productDAO) throws ServletException, IOException {
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String price = request.getParameter("price");
        String productYear = request.getParameter("productYear");
        String image = request.getParameter("image");
        String categoryId = request.getParameter("category");

        CreateProductDTO productDTO = new CreateProductDTO(name, price, productYear, image, categoryId);
        try {
            productDTO.validate();
            Category category = categoryDAO.getById(Integer.parseInt(categoryId));
            if (category == null) {
                throw new InvalidDataException("Category not found!");
            }

            Product product = new Product(name, Float.parseFloat(price), Integer.parseInt(productYear), image, category);
            product.setId(Integer.parseInt(id));
            
            boolean isOk = productDAO.update(product);
            if (!isOk) {
                throw new InvalidDataException("Cannot update product!");
            }
            response.sendRedirect(LIST);
        } catch (ValidationException | InvalidDataException ex) {
            request.setAttribute("msg", ex.getMessage());
            request.getRequestDispatcher(PREPARE_UPDATE).forward(request, response);
        }
    }
    
    private void delete(HttpServletRequest request, HttpServletResponse response, ProductDAO productDAO) throws ServletException, IOException {
        String idRaw = request.getParameter("id");
        try {
            int id = Integer.parseInt(idRaw);
            boolean isOk = productDAO.delete(id);
            if (!isOk) {
                throw new InvalidDataException("Cannot delete product!");
            }
            response.sendRedirect(LIST);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid product ID!");
        }
    }
}
