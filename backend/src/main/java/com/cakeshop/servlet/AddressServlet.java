package com.cakeshop.servlet;

import com.cakeshop.dao.AddressDao;
import com.cakeshop.model.Address;
import com.cakeshop.model.User;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 收货地址Servlet
 */
@WebServlet("/api/address/*")
public class AddressServlet extends BaseServlet {

    private AddressDao addressDao = new AddressDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 获取地址列表
                List<Address> addresses = addressDao.findByUserId(user.getId());
                success(response, addresses);
            } else if (pathInfo.equals("/default")) {
                // 获取默认地址
                Address address = addressDao.findDefaultByUserId(user.getId());
                success(response, address);
            } else {
                // 获取单个地址
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    Address address = addressDao.findById(id);
                    if (address == null || !address.getUserId().equals(user.getId())) {
                        error(response, 404, "地址不存在");
                        return;
                    }
                    success(response, address);
                } catch (NumberFormatException e) {
                    error(response, 400, "无效的地址ID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            JsonObject json = readJsonBody(request);

            Address address = new Address();
            address.setUserId(user.getId());
            address.setReceiverName(getJsonString(json, "receiverName"));
            address.setPhone(getJsonString(json, "phone"));
            address.setProvince(getJsonString(json, "province"));
            address.setCity(getJsonString(json, "city"));
            address.setDistrict(getJsonString(json, "district"));
            address.setDetail(getJsonString(json, "detail"));
            address.setIsDefault(getJsonBoolean(json, "isDefault"));

            // 参数校验
            if (isBlank(address.getReceiverName())) {
                error(response, 400, "收货人姓名不能为空");
                return;
            }
            if (isBlank(address.getPhone())) {
                error(response, 400, "联系电话不能为空");
                return;
            }
            if (!isValidPhone(address.getPhone())) {
                error(response, 400, "联系电话格式不正确");
                return;
            }
            if (isBlank(address.getProvince()) || isBlank(address.getCity()) || isBlank(address.getDistrict())) {
                error(response, 400, "请选择完整的省市区");
                return;
            }
            if (isBlank(address.getDetail())) {
                error(response, 400, "详细地址不能为空");
                return;
            }

            int id = addressDao.insert(address);
            if (id > 0) {
                address.setId(id);
                success(response, "添加成功", address);
            } else {
                error(response, 500, "添加失败，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                error(response, 400, "请指定地址ID");
                return;
            }

            // 检查是否是设置默认地址
            if (pathInfo.matches("/\\d+/default")) {
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                Address address = addressDao.findById(id);
                if (address == null || !address.getUserId().equals(user.getId())) {
                    error(response, 404, "地址不存在");
                    return;
                }
                if (addressDao.setDefault(id, user.getId())) {
                    success(response, "设置成功");
                } else {
                    error(response, 500, "设置失败，请重试");
                }
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的地址ID");
                return;
            }

            Address existAddress = addressDao.findById(id);
            if (existAddress == null || !existAddress.getUserId().equals(user.getId())) {
                error(response, 404, "地址不存在");
                return;
            }

            JsonObject json = readJsonBody(request);

            existAddress.setReceiverName(getJsonString(json, "receiverName"));
            existAddress.setPhone(getJsonString(json, "phone"));
            existAddress.setProvince(getJsonString(json, "province"));
            existAddress.setCity(getJsonString(json, "city"));
            existAddress.setDistrict(getJsonString(json, "district"));
            existAddress.setDetail(getJsonString(json, "detail"));
            existAddress.setIsDefault(getJsonBoolean(json, "isDefault"));

            // 参数校验
            if (isBlank(existAddress.getReceiverName())) {
                error(response, 400, "收货人姓名不能为空");
                return;
            }
            if (isBlank(existAddress.getPhone())) {
                error(response, 400, "联系电话不能为空");
                return;
            }
            if (!isValidPhone(existAddress.getPhone())) {
                error(response, 400, "联系电话格式不正确");
                return;
            }
            if (isBlank(existAddress.getProvince()) || isBlank(existAddress.getCity()) || isBlank(existAddress.getDistrict())) {
                error(response, 400, "请选择完整的省市区");
                return;
            }
            if (isBlank(existAddress.getDetail())) {
                error(response, 400, "详细地址不能为空");
                return;
            }

            if (addressDao.update(existAddress)) {
                success(response, "修改成功");
            } else {
                error(response, 500, "修改失败，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                error(response, 400, "请指定地址ID");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的地址ID");
                return;
            }

            Address address = addressDao.findById(id);
            if (address == null || !address.getUserId().equals(user.getId())) {
                error(response, 404, "地址不存在");
                return;
            }

            if (addressDao.delete(id)) {
                success(response, "删除成功");
            } else {
                error(response, 500, "删除失败，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }
}
