package reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import reggie.dto.SetmealDto;
import reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐, 同时保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐, 同时删除套餐和菜品的关联关系
     *
     * @param ids
     */
    public void deleteWithDish(List<Long> ids);
}
