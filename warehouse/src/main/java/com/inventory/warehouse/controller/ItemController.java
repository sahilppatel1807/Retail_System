package com.inventory.warehouse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.warehouse.entity.Item;
import com.inventory.warehouse.service.ItemService;

@RestController
@RequestMapping("/api/warehouse")
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public Item create(@RequestBody Item item) {
        return service.createItem(item);
    }

    @GetMapping("/all")
    public List<Item> getAllItem() {
        return service.getAllItems();
    }

    @GetMapping("/{id}")
    public Item getItem(@PathVariable Long id) {
        return service.findItem(id);
    }

    @PostMapping("/buy")
    public Item buyItem(
            @RequestParam Long itemId,
            @RequestParam int quantity) {
        return service.sellItem(itemId, quantity);
    }

    @PutMapping("/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item item) {
        return service.updateItem(id, item);
    }

}
