package com.vcs.service;

import com.vcs.model.User;
import com.vcs.model.Role;
import com.vcs.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class UserService {

    public List<User> getAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();
        }
    }

    public void registerUser(String username, String password, Role role) throws Exception {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            user.setRole(role);
            session.persist(user);
            tx.commit();
        }
    }

    public void updateUserRole(Long userId, Role newRole) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) {
                user.setRole(newRole);
                session.merge(user);
            }
            tx.commit();
        }
    }

    public void deleteUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) session.remove(user);
            tx.commit();
        }
    }
}