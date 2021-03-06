package vn.team.freechat_kotlin.request

import com.tvd12.ezyfoxserver.client.entity.EzyData;
import com.tvd12.ezyfoxserver.client.factory.EzyEntityFactory;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import vn.team.freechat_kotlin.constant.Commands

/**
 * Created by tavandung12 on 10/3/18.
 */

class GetContactsRequest : EzyRequest {

    private var skip : Int? = 0;
    private var limit: Int? = 0;

    constructor(skip: Int, limit: Int) {
        this.skip = skip;
        this.limit = limit;
    }

    override fun serialize() : EzyData {
        return EzyEntityFactory.newObjectBuilder()
                .append("skip", skip)
                .append("limit", limit)
                .build();
    }

    override fun getCommand() : String {
        return Commands.CHAT_GET_CONTACTS;
    }
}
