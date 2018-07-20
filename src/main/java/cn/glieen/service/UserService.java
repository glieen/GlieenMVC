package cn.glieen.service;

import cn.glieen.annotation.Service;
import cn.glieen.entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("UserService")
public class UserService {
    private Map<String, User> userMap = new HashMap<>();

    public UserService() {
        userMap.put("glieen", new User("glieen", 23));
        userMap.put("Alice", new User("Alice", 22));
    }

    public User get(String name) {
        return userMap.get(name);
    }

    public List<User> list() {
        List<User> list = new ArrayList<>();
        for (Map.Entry<String, User> entry : userMap.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }
}
