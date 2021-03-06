/**
 * Copyright (C) 2013 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.andstatus.app;

import android.view.View;

import org.andstatus.app.data.AvatarDrawable;

/**
 * One message row
 */
class ConversationOneMessage implements Comparable<ConversationOneMessage> {
    long id;
    long inReplyToMsgId = 0;
    long createdDate = 0;
    long linkedUserId = 0;
    boolean favorited = false;
    String author = "";
    
    /**
     * Comma separated list of the names of all known rebloggers of the message
     */
    String rebloggersString = "";
    String body = "";
    String via = "";
    String inReplyToName = "";
    String recipientName = "";

    /** Numeration starts from 0 **/
    int listOrder = 0;
    /**
     * This order is reverse to the {@link #listOrder}. 
     * First message in the conversation has it == 1.
     * The number is visible to the user.
     */
    int historyOrder = 0;
    int nReplies = 0;
    int nParentReplies = 0;
    int indentLevel = 0;
    int replyLevel = 0;
    
    AvatarDrawable avatarDrawable;
    View view = null;
    
    public ConversationOneMessage(long idIn, int replyLevelIn) {
        this.id = idIn;
        this.replyLevel = replyLevelIn;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConversationOneMessage)) {
            return false;
        }
        ConversationOneMessage row = (ConversationOneMessage) o;
        return id == row.id;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

    /**
     * The newest replies are first, "branches" look up
     */
    @Override
    public int compareTo(ConversationOneMessage another) {
        int compared = listOrder - another.listOrder;
        if (compared == 0) {
            if (createdDate == another.createdDate) {
                if ( id == another.id) {
                    compared = 0;
                } else {
                    compared = (another.id - id > 0 ? 1 : -1);
                }
            } else {
                compared = (another.createdDate - createdDate > 0 ? 1 : -1);
            }
        }
        return compared;
    }
}