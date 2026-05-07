# 构建阶段
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app

# 拷贝Maven配置（阿里云镜像加速）
COPY settings.xml /root/.m2/settings.xml

# 先拷贝pom.xml，利用Docker层缓存
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 拷贝源码并构建
COPY src ./src
RUN mvn package -DskipTests -B

# 运行阶段
FROM tomcat:9-jdk11

# 删除默认应用
RUN rm -rf /usr/local/tomcat/webapps/*

# 拷贝WAR包并解压（避免volume挂载导致的解压问题）
COPY --from=build /app/target/ROOT.war /tmp/ROOT.war
RUN mkdir -p /usr/local/tomcat/webapps/ROOT && \
    cd /usr/local/tomcat/webapps/ROOT && \
    jar -xf /tmp/ROOT.war && \
    rm /tmp/ROOT.war

# 创建上传目录
RUN mkdir -p /usr/local/tomcat/webapps/ROOT/uploads/cakes

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080

CMD ["catalina.sh", "run"]
