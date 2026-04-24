package com.vcs;

import com.vcs.model.Role;
import com.vcs.model.User;
import com.vcs.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

public class CreateAdmin {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);

            session.persist(admin);
            tx.commit();

            System.out.println("УСПЕХ: Администраторът е създаден с истински хеш!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.getSessionFactory().close();
        }
    }
}