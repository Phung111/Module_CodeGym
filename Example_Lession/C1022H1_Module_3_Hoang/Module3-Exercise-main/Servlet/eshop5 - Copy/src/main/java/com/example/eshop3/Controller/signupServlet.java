package com.example.eshop3.Controller;

import com.example.eshop3.Model.User;
import com.example.eshop3.Servlet.IUserService;
import com.example.eshop3.Servlet.implement.UserService;
import utils.AppUtils;
import utils.ValidateUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 50, // 50MB
        maxRequestSize = 1024 * 1024 * 50) // 50MB

@WebServlet(name = "SignupServlet", urlPatterns = "/signup")
public class SignupServlet extends HttpServlet {
    private IUserService userService = new UserService();

    private LoginServlet loginServlet = new LoginServlet();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Server path: " + this.getServletContext().getRealPath("/"));
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/signup.jsp");
        dispatcher.forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {

        User newUser = new User();
        RequestDispatcher requestDispatcher;
        List<String> errors = new ArrayList<>();
        String id = "US" + System.currentTimeMillis() / 1000;
        newUser.setId(id);
        validateFullName(request, errors, newUser);
        validateBirthday(request, errors, newUser);
        validatePhonenumber(request, errors, newUser);
        validateEmail(request, errors, newUser);
        String address = request.getParameter("address");
        newUser.setAddress(address);
        int role = 2;
        newUser.setRole(role);
        validatePassword(request, errors, newUser);
        newUser.setCreatedTime(LocalDateTime.now());
        String image = request.getParameter("file");
        for (Part part : request.getParts()) {
            System.out.println("Content type of Part: " + part.getContentType());
            System.out.println("Name of Part: " + part.getName());
            if (part.getName().equals("file")) {
                String fileName = extractFileName(part);
                fileName = new File(fileName).getName();
//                part.write(this.getFolderUpload().getAbsolutePath() + File.separator + fileName);
                if (fileName.isEmpty()) {
                    newUser.setImage("image\\avatar.jpg");
                } else {
                    String servletPath = ("image\\" +fileName);
                    part.write("G:\\Module3\\Exercise\\Servlet\\eshop5\\src\\main\\webapp\\image\\" +fileName);
                    String servletRealPath = this.getServletContext().getRealPath("/") + "\\image\\" + fileName;
                    System.out.println("servletRealPath...................: " + servletRealPath);
                    part.write(servletRealPath);
                    newUser.setImage(servletPath);
                }
            }
        }


        if (errors.isEmpty()) {
            userService.insertUser(newUser);
            HttpSession httpSession = request.getSession();
            httpSession.setAttribute("user",newUser);
            HttpSession httpSessionmess = request.getSession();
            httpSessionmess.setAttribute("usermess",newUser);
            request.setAttribute("message", "Sign Up Success");
            loginServlet.doPost(request,resp);
//            requestDispatcher = request.getRequestDispatcher("/WEB-INF/login.jsp");
//            requestDispatcher.forward(request, resp);
        } else {
            request.setAttribute("newUser", newUser);
            request.setAttribute("errors", errors);
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/signup.jsp");
            requestDispatcher.forward(request, resp);

        }

    }

    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";
    }
    public File getFolderUpload() {
        File folderUpload = new File(System.getProperty("user.home") + "/Uploads");
        System.out.println(System.getProperty("user.home") + "/Uploads");
        if (!folderUpload.exists()) {
            folderUpload.mkdirs();
        }
        return folderUpload;
    }

    private void validatePassword(HttpServletRequest request, List<String> errors, User newUser) {
        String password = request.getParameter("password");
        String password1 = request.getParameter("password1");
        if (!ValidateUtils.isPasswordValid(password)) {
            errors.add("M???t kh???u kh??ng ????ng ?????nh d???ng. M???t kh???u bao g???m 1 ch??? hoa, 1 s???, v?? 1 k?? t??? ?????c bi???t");
        }
        if (!password.equals(password1)) {
            errors.add("M???t kh???u kh??ng tr??ng");
        }
        newUser.setPassword(password);
    }

    private void validateEmail(HttpServletRequest request, List<String> errors, User newUser) {
        String email = request.getParameter("email");
        if (!ValidateUtils.isEmailValid(email))
            errors.add("Email kh??ng h???p l??? (V?? d???: hoang123@gmail.com)");
        if (userService.selectUserByEmail(email) != null)
            errors.add("Email ???? t???n t???i trong h??? th???ng");
        newUser.setEmail(email);
    }

    private void validatePhonenumber(HttpServletRequest request, List<String> errors, User newUser) {
        String phoneNumber = request.getParameter("phoneNumber");
        try {
            if (!ValidateUtils.isPhoneValid(phoneNumber))
                errors.add("S??? ??i???n tho???i kh??ng h???p l??? (S??? ??i???n tho???i bao g???m 10 ch??? s???, b???t ?????u t??? s??? 0)");
            if (userService.selectUserByPhoneNumber(phoneNumber) != null)
                errors.add("S??? ??i???n tho???i ???? t???n t???i trong h??? th???ng");
            newUser.setPhoneNumber(phoneNumber);
        } catch (NumberFormatException e) {
            errors.add("S??? ??i???n tho???i kh??ng ????ng ?????nh d???ng");
        }

    }

    private void validateBirthday(HttpServletRequest request, List<String> errors, User newUser) {

        try {
            LocalDate birthDay = null;
            birthDay = AppUtils.stringToLocalDate(request.getParameter("birthDay"));
            newUser.setBirthDay(birthDay);
        } catch (Exception e) {
            errors.add("?????nh d???ng ng??y sinh kh??ng h???p l???");
            newUser.setBirthDay(LocalDate.now());
        }
    }

    private void validateFullName(HttpServletRequest request, List<String> errors, User newUser) {
        String fullName = request.getParameter("fullName");
        if (!ValidateUtils.isFullNameValid(fullName)) {
            errors.add("H??? v?? t??n c?? t??? 8-20 k?? t???. Vi???t hoa ch??? c??i ?????u ti??n. v?? d???: Tran Nhat Hoang");
        }
        newUser.setFullName(fullName);
    }
}
