package asia.grails.forum

import org.springframework.security.access.annotation.Secured

class ForumController {
    def springSecurityService
	
    @Secured(['ROLE_USER'])
    def home() {
        [sections:Section.listOrderByTitle()]
    }
	
    @Secured(['ROLE_USER'])
    def topic(long topicId) {
        Topic topic = Topic.get(topicId)

        params.max = 10
        params.sort = 'createDate'
        params.order = 'desc'

        [threads:DiscussionThread.findAllByTopic(topic, params),
         numberOfThreads:DiscussionThread.countByTopic(topic), topic:topic]
    }
	
    @Secured(['ROLE_USER'])
    def thread(long threadId) {
        DiscussionThread thread = DiscussionThread.get(threadId)

        params.max = 10
        params.sort = 'createDate'
        params.order = 'asc'

        [comments:Comment.findAllByThread(thread, params),
         numberOfComments:Comment.countByThread(thread), thread:thread]
    }

    @Secured(['ROLE_USER'])
    def postReply(long threadId, String body) {
        def offset = params.offset
        if (body != null && body.trim().length() > 0) {
            DiscussionThread thread = DiscussionThread.get(threadId)
            def commentBy = springSecurityService.currentUser
            new Comment(thread:thread, commentBy:commentBy, body:body).save()

            // go to last page so user can view his comment
            def numberOfComments = Comment.countByThread(thread)
            def lastPageCount = numberOfComments % 10 == 0 ? 10 : numberOfComments % 10
            offset = numberOfComments - lastPageCount
        }
        redirect(action:'thread', params:[threadId:threadId, offset:offset])
    }
}
