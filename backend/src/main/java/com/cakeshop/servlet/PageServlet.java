package com.cakeshop.servlet;

import com.cakeshop.dao.CakeDao;
import com.cakeshop.dao.CategoryDao;
import com.cakeshop.model.Cake;
import com.cakeshop.model.Category;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 页面路由Servlet（处理JSP页面转发）
 */
@WebServlet({"/", "/cake/*", "/user/*", "/cart", "/order/*", "/admin/*"})
public class PageServlet extends BaseServlet {

    private CakeDao cakeDao = new CakeDao();
    private CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 去掉contextPath前缀，获取相对路径
        String path = requestURI;
        if (contextPath != null && !contextPath.isEmpty()) {
            path = requestURI.substring(contextPath.length());
        }

        try {
            // 首页
            if ("/".equals(path) || "".equals(path)) {
                handleIndex(request, response);
            }
            // 订单列表（放在蛋糕路由之前，优先匹配）
            else if ("/order/list".equals(path)) {
                handleOrderList(request, response);
            }
            // 订单结算页
            else if ("/order/checkout".equals(path)) {
                handleOrderCheckout(request, response);
            }
            // 订单详情
            else if (path.matches("/order/\\d+")) {
                handleOrderDetail(request, response);
            }
            // 蛋糕列表
            else if ("/cake/list".equals(path) || "/cake".equals(path)) {
                handleCakeList(request, response);
            }
            // 蛋糕详情
            else if (path.matches("/cake/\\d+")) {
                handleCakeDetail(request, response);
            }
            // 用户登录
            else if ("/user/login".equals(path)) {
                forward(request, response, "/WEB-INF/views/user/login.jsp");
            }
            // 用户注册
            else if ("/user/register".equals(path)) {
                forward(request, response, "/WEB-INF/views/user/register.jsp");
            }
            // 用户个人中心
            else if ("/user/profile".equals(path)) {
                handleUserProfile(request, response);
            }
            // 用户地址管理
            else if ("/user/address".equals(path)) {
                handleUserAddress(request, response);
            }
            // 购物车
            else if ("/cart".equals(path)) {
                handleCart(request, response);
            }
            // 管理员登录
            else if ("/admin/login".equals(path)) {
                forward(request, response, "/WEB-INF/views/admin/login.jsp");
            }
            // 管理员后台首页
            else if ("/admin".equals(path) || "/admin/".equals(path) ||
                     "/admin/dashboard".equals(path)) {
                handleAdminDashboard(request, response);
            }
            // 管理员蛋糕管理
            else if ("/admin/cake/list".equals(path)) {
                handleAdminCakeList(request, response);
            }
            // 管理员分类管理
            else if ("/admin/category/list".equals(path)) {
                handleAdminCategoryList(request, response);
            }
            // 管理员订单管理
            else if ("/admin/order/list".equals(path)) {
                handleAdminOrderList(request, response);
            }
            // 管理员用户管理
            else if ("/admin/user/list".equals(path)) {
                handleAdminUserList(request, response);
            }
            else {
                error(response, 404, "页面不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "页面加载失败");
        }
    }

    /**
     * 首页
     */
    private void handleIndex(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取热门蛋糕
        List<Cake> hotCakes = cakeDao.findHot(8);
        List<Category> categories = categoryDao.findAll();

        request.setAttribute("cakes", hotCakes);
        request.setAttribute("categories", categories);
        forward(request, response, "/WEB-INF/views/index.jsp");
    }

    /**
     * 蛋糕列表页
     */
    private void handleCakeList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 获取分类列表
        List<Category> categories = categoryDao.findAll();

        // 获取筛选参数
        Integer categoryId = getIntParam(request, "categoryId");
        String keyword = getParam(request, "keyword");
        int page = getIntParam(request, "page", 1);
        int pageSize = 12;

        // 获取蛋糕列表
        List<Cake> cakes = cakeDao.findByPage(categoryId, keyword, page, pageSize);
        int total = cakeDao.count(categoryId, keyword);

        request.setAttribute("categories", categories);
        request.setAttribute("cakes", cakes);
        request.setAttribute("categoryId", categoryId);
        request.setAttribute("keyword", keyword);
        request.setAttribute("page", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("total", total);

        forward(request, response, "/WEB-INF/views/cake/list.jsp");
    }

    /**
     * 蛋糕详情页
     */
    private void handleCakeDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        int id = Integer.parseInt(pathInfo.substring(pathInfo.lastIndexOf('/') + 1));

        Cake cake = cakeDao.findById(id);

        if (cake == null) {
            error(response, 404, "蛋糕不存在");
            return;
        }

        request.setAttribute("cake", cake);
        forward(request, response, "/WEB-INF/views/cake/detail.jsp");
    }

    /**
     * 用户个人中心
     */
    private void handleUserProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkLogin(request, response);
        forward(request, response, "/WEB-INF/views/user/profile.jsp");
    }

    /**
     * 用户地址管理
     */
    private void handleUserAddress(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkLogin(request, response);
        forward(request, response, "/WEB-INF/views/user/address.jsp");
    }

    /**
     * 购物车页面
     */
    private void handleCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, "/WEB-INF/views/cart/cart.jsp");
    }

    /**
     * 订单列表
     */
    private void handleOrderList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkLogin(request, response);
        forward(request, response, "/WEB-INF/views/order/list.jsp");
    }

    /**
     * 订单详情
     */
    private void handleOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkLogin(request, response);
        String pathInfo = request.getPathInfo();
        int id = Integer.parseInt(pathInfo.substring(pathInfo.lastIndexOf('/') + 1));
        request.setAttribute("orderId", id);
        forward(request, response, "/WEB-INF/views/order/detail.jsp");
    }

    /**
     * 管理员仪表盘
     */
    private void handleAdminDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkAdminLogin(request, response);
        forward(request, response, "/WEB-INF/views/admin/dashboard.jsp");
    }

    /**
     * 管理员蛋糕管理
     */
    private void handleAdminCakeList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkAdminLogin(request, response);
        forward(request, response, "/WEB-INF/views/admin/cake/list.jsp");
    }

    /**
     * 管理员分类管理
     */
    private void handleAdminCategoryList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkAdminLogin(request, response);
        forward(request, response, "/WEB-INF/views/admin/category/list.jsp");
    }

    /**
     * 管理员订单管理
     */
    private void handleAdminOrderList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkAdminLogin(request, response);
        forward(request, response, "/WEB-INF/views/admin/order/list.jsp");
    }

    /**
     * 管理员用户管理
     */
    private void handleAdminUserList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkAdminLogin(request, response);
        forward(request, response, "/WEB-INF/views/admin/user/list.jsp");
    }

    /**
     * 订单结算页
     */
    private void handleOrderCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkLogin(request, response);
        forward(request, response, "/WEB-INF/views/order/checkout.jsp");
    }

    /**
     * 检查用户登录
     */
    private void checkLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (getCurrentUser(request) == null) {
            response.sendRedirect(request.getContextPath() + "/user/login");
        }
    }

    /**
     * 检查管理员登录
     */
    private void checkAdminLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        getCurrentUser(request);
        if (getCurrentUser(request) == null || !"admin".equals(getCurrentUser(request).getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
        }
    }

    /**
     * 转发到JSP
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String path)
            throws ServletException, IOException {
        request.getRequestDispatcher(path).forward(request, response);
    }
}
