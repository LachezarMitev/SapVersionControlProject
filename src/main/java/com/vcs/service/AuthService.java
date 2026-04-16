package com.vcs.service;

import com.vcs.model.User;
import com.vcs.util.HibernateUtil;
import com.vcs.util.SessionManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    public boolean login(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            User user = query.uniqueResult();
            
            if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
                SessionManager.getInstance().setCurrentUser(user);
                return true;
            }
        }
        return false;
    }
}