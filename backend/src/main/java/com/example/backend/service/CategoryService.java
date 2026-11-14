package com.example.backend.service;

import com.example.backend.entity.Category;
import com.example.backend.mapper.CategoryMapper;
import com.example.backend.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类服务：构建两级分类树。
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<CategoryVO> tree() {
        List<Category> all = categoryMapper.selectAll();
        List<CategoryVO> roots = new ArrayList<>();
        for (Category category : all) {
            if (category.getParentId() == null || category.getParentId() == 0L) {
                CategoryVO root = new CategoryVO();
                root.setId(category.getId());
                root.setName(category.getName());
                root.setChildren(new ArrayList<>());
                roots.add(root);
            }
        }
        for (Category category : all) {
            if (category.getParentId() != null && category.getParentId() != 0L) {
                for (CategoryVO root : roots) {
                    if (root.getId().equals(category.getParentId())) {
                        CategoryVO child = new CategoryVO();
                        child.setId(category.getId());
                        child.setName(category.getName());
                        root.getChildren().add(child);
                        break;
                    }
                }
            }
        }
        return roots;
    }
}
