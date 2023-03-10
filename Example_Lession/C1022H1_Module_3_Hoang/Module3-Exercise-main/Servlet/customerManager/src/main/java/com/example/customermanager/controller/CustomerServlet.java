package com.example.customermanager.controller;

import com.example.customermanager.exception.CountryInvalidException;
import com.example.customermanager.model.Country;
import com.example.customermanager.model.Customer;
import com.example.customermanager.service.ICountryService;
import com.example.customermanager.service.ICustomerService;
import com.example.customermanager.service.inmemory.CountryService;
import com.example.customermanager.service.inmemory.CustomerService;
import com.example.customermanager.service.jdbc.CountryServiceJDBC;
import com.example.customermanager.service.jdbc.CustomerServiceJDBC;
import com.example.customermanager.utils.ValidateUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/customers"})
public class CustomerServlet extends HttpServlet {

    private ICustomerService customerService;
    private ICountryService countryService;

    @Override
    public void init() throws ServletException {
        customerService = new CustomerServiceJDBC();
        countryService = new CountryServiceJDBC();
        List<Country> countryList = countryService.getAllCountry();
        if (getServletContext().getAttribute("countries") == null) {
            getServletContext().setAttribute("countries", countryList);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "create":
                showFormCreateCustomer(req, resp);
                break;
            case "edit":
                showEditCustomer(req, resp);
                break;
            case "remove":
                showDeleteCustomer(req, resp);
                break;
            default:
                showListCustomer(req, resp);
                break;
        }
    }

    private void showListCustomer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long idCountry = -1;
        String kw = "";
        int numberOfPage = 3;
        int page = 1;

        if (req.getParameter("idCountry") != null) {
            idCountry = Long.parseLong(req.getParameter("idCountry"));
        }
        if (req.getParameter("kw") != null) {
            kw = req.getParameter("kw");
        }
        if (req.getParameter("page") != null) {
            page = Integer.parseInt(req.getParameter("page"));
        }

        List<Customer> customersPagging = customerService.getAllCustomersByKwAndIdCountryPagging(kw, idCountry, (page - 1) * numberOfPage, numberOfPage);


        req.setAttribute("kw", kw);
        req.setAttribute("idCountry", idCountry);
        req.setAttribute("customers", customersPagging);

        HttpSession httpSession = req.getSession();
        req.setAttribute("username", httpSession.getAttribute("username"));
        req.setAttribute("password", httpSession.getAttribute("password"));

        int noOfRecords = customerService.getNoOfRecords();

        int noOfPages = (int) Math.ceil(noOfRecords * 1.0 / numberOfPage);
        req.setAttribute("noOfPages", noOfPages);
        req.setAttribute("currentPage", page);
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/admin/customer/customers.jsp");
        requestDispatcher.forward(req, resp);
    }

    private void showDeleteCustomer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long id = Long.parseLong(req.getParameter("id"));
        Customer customer = customerService.findCustomerById(id);
        req.setAttribute("customer", customer);
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/admin/customer/delete.jsp");
        requestDispatcher.forward(req, resp);
    }

    private void showEditCustomer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long id = Long.parseLong(req.getParameter("id"));
        Customer customer = customerService.findCustomerById(id);
        req.setAttribute("customer", customer);
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/admin/customer/edit.jsp");
        requestDispatcher.forward(req, resp);
    }

    private void showFormCreateCustomer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/admin/customer/create.jsp");
        requestDispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher requestDispatcher;
        String action = req.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "create":
                insertCustomer(req, resp);
                break;
            case "edit": {
                editCustomer(req, resp);
                break;
            }
            case "remove": {
                removeCustomer(req, resp);
//                req.setAttribute("customers", customerService.getAllCustomers());
//                requestDispatcher = req.getRequestDispatcher("/WEB-INF/admin/customer/customers.jsp");
//                requestDispatcher.forward(req, resp);
//                break;
                break;
            }

        }
    }

    private void removeCustomer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id = Long.parseLong(req.getParameter("id"));
        customerService.deleteCustomer(id);
        resp.sendRedirect("/customers");
    }

    private void editCustomer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<String> errors = new ArrayList<>();
        Customer customer = new Customer();
        validateIdView(errors, req, customer);
        validateFullNameView(errors, req, customer);
        validateAddressView(errors, req, customer);
        validateCountryView(errors, req, customer);
        String picture = req.getParameter("picture");
        customer.setPicture(picture);
        RequestDispatcher requestDispatcher = null;
        if (errors.isEmpty()) {
            customerService.updateCustomer(customer);
            resp.sendRedirect("/customers");
        }else{
            req.setAttribute("errors", errors);
            req.setAttribute("customer", customer);
            requestDispatcher = req.getRequestDispatcher("/WEB-INF/admin/customer/edit.jsp");
           requestDispatcher.forward(req, resp);

        }
    }

    private void insertCustomer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<String> errors = new ArrayList<>();
        Customer customer = new Customer();
        validateFullNameView(errors, req, customer);     // false
        validateAddressView(errors, req, customer);      // false
        validateCountryView(errors, req, customer);
        String picture = req.getParameter("picture");
        customer.setPicture(picture);

        if (errors.isEmpty()) {
            customer.setId(customerService.findCustomerByIdMax() + 1);
            customerService.addCustomer(customer);
            req.setAttribute("message", "Them khach hang thanh cong");
        }else{
            req.setAttribute("errors", errors);
            req.setAttribute("customer", customer);
        }

        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/admin/customer/create.jsp");
        requestDispatcher.forward(req, resp);
    }

    private boolean validateIdView(List<String> errors, HttpServletRequest req, Customer customer) {
        try {
            long id = Long.parseLong(req.getParameter("id"));
            if (customerService.findCustomerById(id) == null) {
                throw new NullPointerException("Customer not exists");
            }
            customer.setId(id);
        } catch (NumberFormatException numberFormatException) {
            errors.add("ID customer not valid");
            return false;
        } catch (NullPointerException nullPointerException) {
            errors.add(nullPointerException.getMessage());
            return false;
        }
        return true;

    }

    private void validateCountryView(List<String> errors, HttpServletRequest req, Customer customer) {
        long idCountry = -1;

        try {
            idCountry = Long.parseLong(req.getParameter("idCountry"));
            // kiem tra country co hop le hay khong
            if (countryService.findCountryById(idCountry) == null) {
                throw new CountryInvalidException("Country is exists");
            }
        } catch (NumberFormatException numberFormatException) {
            errors.add("Country is not valid");
        } catch (CountryInvalidException countryInvalidException) {
            errors.add(countryInvalidException.getMessage());
        }
        customer.setIdCountry(idCountry);
    }

    private void validateAddressView(List<String> errors, HttpServletRequest req, Customer customer) {
        String address = req.getParameter("address");
        customer.setAddress(address);
        if (address.equals("")) {
            errors.add("Address is not empty");
        }
    }

    private void validateFullNameView(List<String> errors, HttpServletRequest req, Customer customer) {
        String name = req.getParameter("name");
        customer.setName(name);
        if (name.equals("")) {
            errors.add("Fullname is not empty");
        } else {
            if (ValidateUtils.isFullNameValid(name) == false) {
                errors.add("Fullname not valid. Start with Upcase, least 4 character");
            }
        }
    }

}
