package com.tvd12.freechat.handler;

import static com.tvd12.freechat.constant.ChatCommands.SEARCH_CONTACTS_USERS;

import com.tvd12.ezyfox.bean.annotation.EzyAutoBind;
import com.tvd12.ezyfox.bean.annotation.EzyPrototype;
import com.tvd12.ezyfox.binding.EzyDataBinding;
import com.tvd12.ezyfox.binding.annotation.EzyObjectBinding;
import com.tvd12.ezyfox.collect.Lists;
import com.tvd12.ezyfox.core.annotation.EzyClientRequestListener;
import com.tvd12.ezyfox.core.exception.EzyBadRequestException;
import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.freechat.common.data.ChatUser;
import com.tvd12.freechat.common.service.ChatUserService;
import lombok.Setter;

import java.util.List;

@Setter
@EzyPrototype
@EzyObjectBinding
@EzyClientRequestListener(SEARCH_CONTACTS_USERS)
public class ChatSearchContactsUsersHandler
		extends ChatClientRequestHandler 
		implements EzyDataBinding {

	protected int skip;
	protected int limit;
	private String keyword;

	@EzyAutoBind
	private ChatUserService userService;

	@Override
	protected void preExecute() {
		if(limit > 30)
			limit = 30;
	}

	@Override
	protected void execute() throws EzyBadRequestException {
		logger.debug("searched user by username: {}, skip: {}, limit: {}", keyword, skip, limit);
		if (EzyStrings.isEmpty(keyword)) {
			response(Lists.newArrayList());
			return;
		}
		List<ChatUser> users = userService.getSearchUsers(keyword, user.getName(), skip, limit);
		response(users);
	}

	private void response(List<ChatUser> users) {
		responseFactory.newObjectResponse()
			.command(SEARCH_CONTACTS_USERS)
			.param("users", users)
			.session(session)
			.execute();
	}
}
