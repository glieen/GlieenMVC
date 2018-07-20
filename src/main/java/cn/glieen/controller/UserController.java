package cn.glieen.controller;

import cn.glieen.annotation.Autowired;
import cn.glieen.annotation.Controller;
import cn.glieen.annotation.RequestMapping;
import cn.glieen.annotation.RequestParam;
import cn.glieen.entity.User;
import cn.glieen.service.UserService;

import java.util.List;

@Controller("UserController")
public class UserController {

    @Autowired("UserService")
    private UserService userService;

    @RequestMapping("/user")
    public User user(@RequestParam("name") String name) {
        return userService.get(name);
    }

    @RequestMapping("/users")
    public List<User> users(){
        return userService.list();
    }
}
