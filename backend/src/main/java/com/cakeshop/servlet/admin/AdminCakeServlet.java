package com.cakeshop.servlet.admin;

import com.cakeshop.dao.CakeDao;
import com.cakeshop.model.Cake;
import com.cakeshop.servlet.BaseServlet;
import com.cakeshop.util.JsonUtil;
import com.google.gson.JsonObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 后台蛋糕管理Servlet
 */
@WebServlet("/api/admin/cake/*")
public class AdminCakeServlet extends BaseServlet {

    private CakeDao cakeDao = new CakeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 获取蛋糕列表
                Integer categoryId = getIntParam(request, "categoryId");
                String keyword = getParam(request, "keyword");
                String status = getParam(request, "status");
                int page = getIntParam(request, "page", 1);
                int pageSize = getIntParam(request, "pageSize", 10);

                List<Cake> cakes = cakeDao.findAllByPage(categoryId, keyword, status, page, pageSize);
                int total = cakeDao.countAll(categoryId, keyword, status);

                Map<String, Object> result = JsonUtil.createPageResult(cakes, page, pageSize, total);
                success(response, result);
            } else {
                // 获取蛋糕详情
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    Cake cake = cakeDao.findById(id);
                    if (cake == null) {
                        error(response, 404, "蛋糕不存在");
                        return;
                    }
                    success(response, cake);
                } catch (NumberFormatException e) {
                    error(response, 400, "无效的蛋糕ID");
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
            // 检查是否是文件上传请求
            if (ServletFileUpload.isMultipartContent(request)) {
                handleFileUpload(request, response);
                return;
            }

            JsonObject json = readJsonBody(request);

            Cake cake = new Cake();
            cake.setName(getJsonString(json, "name"));
            cake.setCategoryId(getJsonInt(json, "categoryId"));
            String priceStr = getJsonString(json, "price");
            if (priceStr != null) {
                cake.setPrice(new BigDecimal(priceStr));
            }
            String originalPriceStr = getJsonString(json, "originalPrice");
            if (originalPriceStr != null && !originalPriceStr.isEmpty()) {
                cake.setOriginalPrice(new BigDecimal(originalPriceStr));
            }
            cake.setImage(getJsonString(json, "image"));
            cake.setImages(getJsonString(json, "images"));
            cake.setDescription(getJsonString(json, "description"));
            cake.setSize(getJsonString(json, "size"));
            cake.setFlavor(getJsonString(json, "flavor"));
            cake.setStock(getJsonInt(json, "stock"));
            cake.setStatus(getJsonString(json, "status"));

            // 参数校验
            if (isBlank(cake.getName())) {
                error(response, 400, "商品名称不能为空");
                return;
            }
            if (cake.getCategoryId() == null) {
                error(response, 400, "请选择商品分类");
                return;
            }
            if (cake.getPrice() == null || cake.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                error(response, 400, "价格必须大于0");
                return;
            }

            int id = cakeDao.insert(cake);
            if (id > 0) {
                cake.setId(id);
                success(response, "添加成功", cake);
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
            if (pathInfo == null || pathInfo.equals("/")) {
                error(response, 400, "请指定蛋糕ID");
                return;
            }

            // 处理上下架
            if (pathInfo.matches("/\\d+/status")) {
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                JsonObject json = readJsonBody(request);
                String status = getJsonString(json, "status");

                if (!"on".equals(status) && !"off".equals(status)) {
                    error(response, 400, "无效的状态值");
                    return;
                }

                if (cakeDao.updateStatus(id, status)) {
                    success(response, "on".equals(status) ? "上架成功" : "下架成功");
                } else {
                    error(response, 500, "操作失败，请重试");
                }
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的蛋糕ID");
                return;
            }

            Cake existCake = cakeDao.findById(id);
            if (existCake == null) {
                error(response, 404, "蛋糕不存在");
                return;
            }

            JsonObject json = readJsonBody(request);

            existCake.setName(getJsonString(json, "name"));
            existCake.setCategoryId(getJsonInt(json, "categoryId"));
            String priceStr = getJsonString(json, "price");
            if (priceStr != null) {
                existCake.setPrice(new BigDecimal(priceStr));
            }
            String originalPriceStr = getJsonString(json, "originalPrice");
            if (originalPriceStr != null && !originalPriceStr.isEmpty()) {
                existCake.setOriginalPrice(new BigDecimal(originalPriceStr));
            }
            String image = getJsonString(json, "image");
            if (image != null) {
                existCake.setImage(image);
            }
            existCake.setImages(getJsonString(json, "images"));
            existCake.setDescription(getJsonString(json, "description"));
            existCake.setSize(getJsonString(json, "size"));
            existCake.setFlavor(getJsonString(json, "flavor"));
            Integer stock = getJsonInt(json, "stock");
            if (stock != null) {
                existCake.setStock(stock);
            }
            String status = getJsonString(json, "status");
            if (status != null) {
                existCake.setStatus(status);
            }

            // 参数校验
            if (isBlank(existCake.getName())) {
                error(response, 400, "商品名称不能为空");
                return;
            }
            if (existCake.getCategoryId() == null) {
                error(response, 400, "请选择商品分类");
                return;
            }
            if (existCake.getPrice() == null || existCake.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                error(response, 400, "价格必须大于0");
                return;
            }

            if (cakeDao.update(existCake)) {
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
            if (pathInfo == null || pathInfo.equals("/")) {
                error(response, 400, "请指定蛋糕ID");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的蛋糕ID");
                return;
            }

            Cake cake = cakeDao.findById(id);
            if (cake == null) {
                error(response, 404, "蛋糕不存在");
                return;
            }

            if (cakeDao.delete(id)) {
                success(response, "删除成功");
            } else {
                error(response, 500, "删除失败，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    /**
     * 处理文件上传
     */
    private void handleFileUpload(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1024 * 1024); // 1MB
            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(5 * 1024 * 1024); // 5MB
            upload.setSizeMax(10 * 1024 * 1024); // 10MB

            List<FileItem> items = upload.parseRequest(request);

            for (FileItem item : items) {
                if (!item.isFormField() && item.getSize() > 0) {
                    String fileName = item.getName();
                    String ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

                    // 验证文件类型
                    if (!".jpg".equals(ext) && !".jpeg".equals(ext) && !".png".equals(ext) && !".gif".equals(ext)) {
                        error(response, 400, "只支持jpg/jpeg/png/gif格式图片");
                        return;
                    }

                    // 生成新文件名
                    String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

                    // 保存文件
                    String uploadDir = getServletContext().getRealPath("/uploads/cakes");
                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    dir.setReadable(true, false);
                    dir.setExecutable(true, false);

                    File file = new File(dir, newFileName);
                    item.write(file);
                    file.setReadable(true, false);

                    // 返回文件路径
                    String filePath = "/uploads/cakes/" + newFileName;
                    success(response, "上传成功", filePath);
                    return;
                }
            }

            error(response, 400, "请选择要上传的文件");
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "上传失败，请重试");
        }
    }
}
