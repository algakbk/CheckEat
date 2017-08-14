package application.servlets;

import application.logic.*;
import application.utils.*;
import com.google.gson.Gson;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
@MultipartConfig
public class ProfileServlet extends HttpServlet {

    private EntityManager em;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManagerFactory emf = (EntityManagerFactory) getServletContext().getAttribute("emf");
        em = emf.createEntityManager();
        request.setCharacterEncoding("UTF-8");
        String requestType = request.getParameter(Constants.REQUEST_TYPE);

        if (requestType.equals(Constants.GET_CUSTOMER)) {
            getCustomer(request, response);
        }
        else if(requestType.equals(Constants.GET_RESTAURANT)) {
            getRestaurant(request, response);
        }
        else if (requestType.equals("checkIfRestaurant")) {
            checkIfRestaurant(request,response);
        }
        else if (requestType.equals("favorites")) {
            addAndRemoveFavorites(request,response);
        }
        else if (requestType.equals("getDish")) {
            getDish(request,response);
        }
        else if (requestType.equals("editDish")) {
            editDish(request,response);
        }
        else if (requestType.equals("editCustomer")) {
            editCustomer(request,response);
        }       
        else if (requestType.equals("editRestaurant")) {
            editRestaurant(request,response);
        }
        else if (requestType.equals("like")) {
            likeAndDisLike(request,response);
        }
        else if (requestType.equals("comments")){
            addComment(request,response);
        }
        else if (requestType.equals("checkIfCustomerOrRestaurant")){
            checkIfCustomerOrRestaurant(request,response);
        }
        else if (requestType.equals("deleteDish")) {
            deleteDish(request,response);
        }
    }

    private void deleteDish(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getParameter(request, Constants.USERNAME);
        Dish dish = em.find(Dish.class,Integer.parseInt(request.getParameter("dishId")));
        Customer customer = em.find(Customer.class, usernameFromSession);
        if (customer != null) {
            em.getTransaction().begin();
            customer.getAddedDishes().remove(dish);
            dish.getRestaurant().getDishes().remove(dish);
            em.remove(dish);
            em.getTransaction().commit();
        }
        else {
            em.getTransaction().begin();
            dish.getRestaurant().getDishes().remove(dish);
            em.remove(dish);
            em.getTransaction().commit();
        }
    }

    private void checkIfCustomerOrRestaurant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SignedUser signedUser = em.find(SignedUser.class, request.getParameter("userName"));
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        if (signedUser != null) {
            if (signedUser.getType() == SignedUser.Type.RESTAURANT)
                out.print(true);
            else
                out.print(false);
            out.flush();
        }
    }

    private void addComment(HttpServletRequest request, HttpServletResponse response) {
        Dish dish = em.find(Dish.class,Integer.parseInt(request.getParameter("dishId")));
        Date date = new Date();
        em.getTransaction().begin();
        dish.getComments().put(date.toString(),request.getParameter("comment"));
        em.getTransaction().commit();
    }

    private void likeAndDisLike(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getParameter(request, Constants.USERNAME);
        Dish dish = em.find(Dish.class,Integer.parseInt(request.getParameter("dishId")));
        if(usernameFromSession != null) {
            Customer customer = em.find(Customer.class, usernameFromSession);
            if (customer!=null) { // the user is customer
                if (!customer.getLikedDished().contains(dish.getId())) {
                    em.getTransaction().begin();
                    customer.getLikedDished().add(dish.getId());
                    dish.setNumLikes(dish.getNumLikes()+1);
                    em.getTransaction().commit();
                }
                else {
                    em.getTransaction().begin();
                    customer.getLikedDished().remove((Integer)dish.getId());
                    dish.setNumLikes(dish.getNumLikes()-1);
                    em.getTransaction().commit();
                }
            }
        }
        else {
            em.getTransaction().begin();
            dish.setNumLikes(dish.getNumLikes()+1);
            em.getTransaction().commit();
        }
    }

    private void editRestaurant(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String usernameFromSession = SessionUtils.getParameter(request, Constants.USERNAME);
        boolean isAdmin = false;
        if (usernameFromSession.equals("CheckEat")) {
            usernameFromSession = request.getParameter("restUserName");
            isAdmin = true;
        }
        Restaurant restaurant = em.find(Restaurant.class,usernameFromSession);
        String logoUrl = ServletUtils.uploadImageToCloudinary(request.getPart("image"));
        if (logoUrl==null)
            logoUrl=ServletUtils.loadImageURL(request.getParameter("imageURL"));
        em.getTransaction().begin();
        restaurant.setRestaurantName(request.getParameter(Constants.REST_NAME));
        restaurant.setCity(request.getParameter(Constants.REST_CITY));
        restaurant.setStreet(request.getParameter("restStreet"));
        restaurant.setStreetNum(request.getParameter("restStreetNum"));
        restaurant.setEmail(request.getParameter(Constants.EMAIL));
        restaurant.setPhone(request.getParameter(Constants.PHONE));
        restaurant.setContactName(request.getParameter(Constants.CONTACT_NAME));
        restaurant.setContactPhone(request.getParameter(Constants.CONTACT_PHONE));
        restaurant.setLink(request.getParameter(Constants.REST_LINK));
        if (!logoUrl.equals(""))
            restaurant.setLogoUrl(logoUrl);
        em.getTransaction().commit();
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(isAdmin);
        out.flush();
    }

    private void editCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getParameter(request, Constants.USERNAME);
        boolean isAdmin = false;
        if (usernameFromSession.equals("CheckEat")) {
            usernameFromSession = request.getParameter("customerUserName");
            isAdmin = true;
        }
        Customer customer = em.find(Customer.class,usernameFromSession);
        em.getTransaction().begin();
        customer.setEmail(request.getParameter(Constants.EMAIL));
        customer.setPhone(request.getParameter(Constants.PHONE));
        em.getTransaction().commit();
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(isAdmin);
        out.flush();
    }

    private void editDish(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Dish dish = em.find(Dish.class,Integer.parseInt(request.getParameter("dishId")));
        String logoUrl = ServletUtils.uploadImageToCloudinary(request.getPart("image"));
        if (logoUrl==null)
            logoUrl=ServletUtils.loadImageURL(request.getParameter("imageURL"));
        em.getTransaction().begin();
        dish.setDishName(request.getParameter(Constants.DISH_NAME));
        dish.setDishName(request.getParameter(Constants.DISH_NAME));
        dish.setSpecialTypes(request.getParameterValues(Constants.SPECIAL_TYPES));
        dish.setOtherTypes(request.getParameterValues(Constants.OTHER_TYPES));
        dish.setIngredients(request.getParameterValues(Constants.INGREDIENTS));
        if (!logoUrl.equals(""))
            dish.setDishUrl(logoUrl);
        em.getTransaction().commit();
    }

    private void getDish(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Dish dish = em.find(Dish.class,Integer.parseInt(request.getParameter("dishId")));
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        String json = gson.toJson(new GsonDish(dish));
        out.println(json);
        out.flush();
    }

    private void addAndRemoveFavorites(HttpServletRequest request, HttpServletResponse response) {
        String usernameFromSession = SessionUtils.getParameter(request, Constants.USERNAME);
        if(usernameFromSession != null) {
            Customer customer = em.find(Customer.class,usernameFromSession);
            if (customer!=null) { // the user is customer
                Dish dish = em.find(Dish.class,Integer.parseInt(request.getParameter("dishId")));
                if (!customer.getFavoritesDishes().contains(dish)) {
                    em.getTransaction().begin();
                    customer.getFavoritesDishes().add(dish);
                    em.getTransaction().commit();
                }
                else {
                    em.getTransaction().begin();
                    customer.getFavoritesDishes().remove(dish);
                    em.getTransaction().commit();
                }
            }
        }
    }

    private void checkIfRestaurant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getParameter(request, Constants.USERNAME);
        Restaurant restaurant = em.find(Restaurant.class,usernameFromSession);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        if (restaurant!=null) {
            String json = gson.toJson(new GsonRestaurant(restaurant));
            out.println(json);
        }
        out.flush();
    }

    private void getCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getParameter(request, Constants.USERNAME);
        if (usernameFromSession.equals("CheckEat"))
            usernameFromSession = request.getParameter("customerUserName");
        Customer customer = em.find(Customer.class,usernameFromSession);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        String json = gson.toJson(new GsonCustomer(customer));
        out.println(json);
        out.flush();
    }

    private void getRestaurant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getParameter(request, Constants.USERNAME);
        if (usernameFromSession.equals("CheckEat"))
            usernameFromSession = request.getParameter("restUserName");
        Restaurant restaurant = em.find(Restaurant.class,usernameFromSession);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        String json = gson.toJson(new GsonRestaurant(restaurant));
        out.println(json);
        out.flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}