package com.ershi.dahu.mapper;

import com.ershi.dahu.model.dto.statistic.AnswerResultCountDTO;
import com.ershi.dahu.model.dto.statistic.AppAnswerCountDTO;
import com.ershi.dahu.model.entity.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author qingtian_jun
* @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
* @createDate 2024-07-07 20:40:28
* @Entity com.ershi.dahu.model.entity.UserAnswer
*/
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    /**
     * 统计查询每个应用用户答题情况（top10）
     * @return {@link List}<{@link AnswerResultCountDTO}>
     */
    @Select("select appId, app.appName, count(user_answer.userId) as userAnswerCount from user_answer\n" +
            "JOIN app ON user_answer.appId = app.Id\n" +
            "where app.isDelete = 0\n"+
            "group by appId\n" +
            "order by userAnswerCount\n" +
            "asc limit 10;")
    List<AppAnswerCountDTO> getAppAnswerCount();


    /**
     * 统计指定应用中结果情况
     * @param appId
     * @return {@link List}<{@link AnswerResultCountDTO}>
     */
    @Select("select resultName, count(resultName) as resultCount\n" +
            "from user_answer\n" +
            "where appId = #{appId}\n" +
            "and isDelete = 0\n"+
            "group by resultName\n" +
            "order by resultCount desc;")
    List<AnswerResultCountDTO> getAnswerResultCount(Long appId);
}




