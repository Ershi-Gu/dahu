package com.ershi.dahu.job.once;

import com.ershi.dahu.esdao.PostEsDao;
import com.ershi.dahu.model.dto.post.PostEsDTO;
import com.ershi.dahu.model.entity.Post;
import com.ershi.dahu.service.PostService;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.springframework.boot.CommandLineRunner;

/**
 * 全量同步帖子到 es
 *
 * @author <a href="https://github.com/Ershi-Gu">Ershi-Gu</a>
 * 
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class FullSyncPostToEs implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Resource
    private PostEsDao postEsDao;

    @Override
    public void run(String... args) {
        List<Post> postList = postService.list();
        if (CollUtil.isEmpty(postList)) {
            return;
        }
        List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
        final int pageSize = 500;
        int total = postEsDTOList.size();
        log.info("FullSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            postEsDao.saveAll(postEsDTOList.subList(i, end));
        }
        log.info("FullSyncPostToEs end, total {}", total);
    }
}
