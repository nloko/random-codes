package com.nloko.animationtest;

import net.rim.device.api.ui.Field;

/**
 * Encapsulate animation state information within each animated field
 * @author Neil
 *
 */
public interface AnimatedField {
	/**
	 * The actual field being animated
	 * @return
	 */
	Field getField();
	/**
	 * Update the current field position based on the progress of an animation
	 * @param time
	 * @param duration
	 */
	void updatePosition(int time, int duration);
	/**
	 * Get the current position of the field
	 * @return
	 */
	Point getCurrentPosition();
}
