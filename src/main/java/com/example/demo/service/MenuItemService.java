package com.example.demo.service;

import com.example.demo.entity.MenuItem;
import java.util.List;

public interface MenuItemService {
    MenuItem createMenuItem(MenuItem menuItem);
    MenuItem updateMenuItem(Long id, MenuItem menuItem);
    MenuItem getMenuItemById(Long id);
    List<MenuItem> getAllMenuItems();
    void deactivateMenuItem(Long id);
}