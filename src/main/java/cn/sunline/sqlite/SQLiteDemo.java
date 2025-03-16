package cn.sunline.sqlite;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.simple.SimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class SQLiteDemo {
    // 数据库连接URL
    private static final String DB_URL = "jdbc:sqlite:tool_db.db";
    private static final DataSource ds = new SimpleDataSource(DB_URL, null, null);
    
    // 创建用户表
    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE," +
                "age INTEGER" +
                ")";
        
        try {
            Db.use(ds).execute(sql);
            System.out.println("用户表创建成功！");
        } catch (SQLException e) {
            System.out.println("创建表失败: " + e.getMessage());
        }
    }

    // 插入用户数据
    public static void insertUser(String name, String email, int age) {
        try {
            Entity entity = Entity.create("users")
                    .set("name", name)
                    .set("email", email)
                    .set("age", age);
            
            Db.use(ds).insert(entity);
            System.out.println("用户数据插入成功！");
        } catch (SQLException e) {
            System.out.println("插入数据失败: " + e.getMessage());
        }
    }

    // 查询所有用户
    public static void queryAllUsers() {
        try {
            List<Entity> users = Db.use(ds).findAll("users");
            
            System.out.println("\n所有用户信息：");
            System.out.println("ID\t姓名\t邮箱\t\t年龄");
            System.out.println("----------------------------------------");
            
            for (Entity user : users) {
                System.out.printf("%d\t%s\t%s\t%d%n",
                    user.getLong("id"),
                    user.getStr("name"),
                    user.getStr("email"),
                    user.getInt("age")
                );
            }
        } catch (SQLException e) {
            System.out.println("查询数据失败: " + e.getMessage());
        }
    }

    // 更新用户信息
    public static void updateUser(int id, String name, String email, int age) {
        try {
            Entity entity = Entity.create("users")
                    .set("name", name)
                    .set("email", email)
                    .set("age", age);
            
            int updated = Db.use(ds).update(
                entity,
                Entity.create("users").set("id", id)
            );
            
            if (updated > 0) {
                System.out.println("用户信息更新成功！");
            } else {
                System.out.println("未找到ID为 " + id + " 的用户！");
            }
        } catch (SQLException e) {
            System.out.println("更新数据失败: " + e.getMessage());
        }
    }

    // 删除用户
    public static void deleteUser(int id) {
        try {
            int deleted = Db.use(ds).del(
                Entity.create("users").set("id", id)
            );
            
            if (deleted > 0) {
                System.out.println("用户删除成功！");
            } else {
                System.out.println("未找到ID为 " + id + " 的用户！");
            }
        } catch (SQLException e) {
            System.out.println("删除数据失败: " + e.getMessage());
        }
    }

    // 根据ID查询用户
    public static void queryUserById(int id) {
        try {
            Entity user = Db.use(ds).get(
                Entity.create("users").set("id", id)
            );
            
            if (user != null) {
                System.out.println("\n用户信息：");
                System.out.println("ID: " + user.getLong("id"));
                System.out.println("姓名: " + user.getStr("name"));
                System.out.println("邮箱: " + user.getStr("email"));
                System.out.println("年龄: " + user.getInt("age"));
            } else {
                System.out.println("未找到ID为 " + id + " 的用户！");
            }
        } catch (SQLException e) {
            System.out.println("查询数据失败: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // 创建表
        createTable();

        // 插入测试数据
        System.out.println("\n插入测试数据：");
        insertUser("张三", "zhangsan@example.com", 25);
        insertUser("李四", "lisi@example.com", 30);
        insertUser("王五", "wangwu@example.com", 35);

        // 查询所有用户
        queryAllUsers();

        // 更新用户信息
        System.out.println("\n更新用户信息：");
        updateUser(1, "张三丰", "zhangsanfeng@example.com", 26);

        // 查询特定用户
        System.out.println("\n查询特定用户：");
        queryUserById(1);

        // 删除用户
        System.out.println("\n删除用户：");
        deleteUser(2);

        // 再次查询所有用户
        queryAllUsers();
    }
} 