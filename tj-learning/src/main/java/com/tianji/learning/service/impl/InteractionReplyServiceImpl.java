package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.service.IInteractionReplyService;
import com.tianji.learning.mapper.InteractionReplyMapper;
import org.springframework.stereotype.Service;

/**
* @author smile67
* @description 针对表【interaction_reply(互动问题的回答或评论)】的数据库操作Service实现
* @createDate 2024-08-14 17:20:57
*/
@Service
public class InteractionReplyServiceImpl extends ServiceImpl<InteractionReplyMapper, InteractionReply>
    implements IInteractionReplyService {

}




