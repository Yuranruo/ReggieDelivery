package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reggie.common.R;
import reggie.dto.DishDto;
import reggie.entity.Category;
import reggie.entity.Dish;
import reggie.entity.DishFlavor;
import reggie.entity.Employee;
import reggie.service.CategoryService;
import reggie.service.DishFlavorService;
import reggie.service.DishService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //dishService查询出的结果只有菜品, 没有菜品分类, 使用DishDto可以查询到categoryName
        Page<DishDto> dishDtoPage = new Page<>();

        //构造条件构造器: 过滤条件，排序条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //查询所有菜品
        dishService.page(pageInfo, queryWrapper);

        //将查询结果存入dishDtoPage中，除了records
        //records中存入的就是dishService中查询出来的所有结果，类型是list
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //单独提取records
        List<Dish> records = pageInfo.getRecords();

        //遍历records，使用records中的categoryId到categoryService中查询相应category，并将categoryName放到dishDto中
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();   //dishDtoPage需要传入dishDto对象
            BeanUtils.copyProperties(item, dishDto); //dishDto是空的，需要将刚才查询出来的item放进去

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;

        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("新增分类, 分类信息: {}", dishDto.toString());

        //保存分类
        dishService.saveWithFlaver(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 根据id修改分类信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        log.info("根据id修改分类信息");

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 更新菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("新增分类, 分类信息: {}", dishDto.toString());

        //保存分类
        dishService.updateWithFlaver(dishDto);

        return R.success("更新菜品成功");
    }

    /**
     * 根据条件查询对应菜品数据
     * 参数用Dish不用Long id的原因是用dish可以传更多类型的数据, 如id, name等
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        //构造查询条件和排序条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, 1);    //仅查询启售的菜品
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        //遍历records，使用records中的categoryId到categoryService中查询相应category，并将categoryName放到dishDto中
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();   //dishDtoPage需要传入dishDto对象
            BeanUtils.copyProperties(item, dishDto); //dishDto是空的，需要将刚才查询出来的item放进去

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //获得id
            Long itemId = item.getId();
            //通过id查询口味信息
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, itemId);
            List<DishFlavor> flavorList = dishFlavorService.list(lambdaQueryWrapper);
            //将查询出的口味信息保存到dishDto中
            dishDto.setFlavors(flavorList);
            return dishDto;

        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }

}
