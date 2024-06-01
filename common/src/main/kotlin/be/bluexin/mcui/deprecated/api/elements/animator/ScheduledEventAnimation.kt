package be.bluexin.mcui.deprecated.api.elements.animator

/**
 * This is a copy from the library LibrarianLib
 * This code is covered under GNU Lesser General Public License v3.0
 */

/**
 * TODO: Document file ScheduledEventAnimation
 *
 * Created by TheCodeWarrior
 */
class ScheduledEventAnimation(time: Float, callback: Runnable) :
    Animation<ScheduledEventAnimation.PointlessAnimatableObject>(
        PointlessAnimatableObject,
        AnimatableProperty.get(PointlessAnimatableObject::class.java, "field")
    ) {

    init {
        start = time
        duration = 0f
        completion = callback
    }

    override fun update(time: Float) {}

    object PointlessAnimatableObject {
        var field = 0
    }
}
