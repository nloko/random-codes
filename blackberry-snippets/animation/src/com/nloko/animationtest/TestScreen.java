/**
 * Copyright 2010 Neil Loknath
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
 ***/

package com.nloko.animationtest;

import com.nloko.animationtest.Animation.AnimationListener;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public class TestScreen extends MainScreen {
	private class TestManager extends Manager implements AnimationListener {
		private final Animation _animation;		
		private final int _duration = 1000;
		
		private volatile boolean _isAnimating;
				
		private AnimatedField _hello = new AnimatedField() {
			private final LabelField _field = new LabelField("Hello");
			
			private final int WIDTH = Display.getWidth();
			private final int _sx = -100;
			
			private final Point _pos = new Point(_sx, Display.getHeight() / 2);
			private final Point _dest = new Point(WIDTH - (WIDTH + Font.getDefault().getAdvance("Hello World!")) / 2 , _pos.y);
			
			public Field getField() {
				return _field;
			}
			
			public Point getCurrentPosition() {
				return _pos;
			}

			public void updatePosition(int time, int duration) {
				_pos.x = _sx + (_dest.x - _sx) * time / duration;
			}
		};
		
		private AnimatedField _world = new AnimatedField() {
			private final LabelField _field = new LabelField("World!");
			
			private final int WIDTH = Display.getWidth();
			private final int OFFSET_FROM_CENTER = WIDTH - (WIDTH + Font.getDefault().getAdvance("Hello World!")) / 2;
			
			private final int _sy = -150;
			
			
			private final Point _pos = new Point(OFFSET_FROM_CENTER + Font.getDefault().getAdvance("Hello "), _sy);
			private final Point _dest = new Point(_pos.x, Display.getHeight() / 2);
			
			public Field getField() {
				return _field;
			}
			
			public Point getCurrentPosition() {
				return _pos;
			}

			public void updatePosition(int time, int duration) {
				_pos.y = _sy + (_dest.y - _sy)  * time / duration;
			}
		};
		
		public TestManager() {
			super(USE_ALL_HEIGHT | USE_ALL_WIDTH);
			
			_animation = new Animation(_duration);
			_animation.setListener(this);
			_animation.setInterpolator(new OvershootInterpolator());
			
			add(_hello.getField());
			add(_world.getField());
		}

		public void startAnimation() {
			_isAnimating = true;
			_animation.start();
		}
		
		protected void sublayout(int maxwidth, int maxheight) {
			updateAnimation(_hello);
			updateAnimation(_world);
			setExtent(maxwidth, maxheight);
		}

		/**
		 * Helper method to update the position of fields
		 * @param af
		 */
		private void updateAnimation(AnimatedField af) {
			Field f = af.getField();
			final int width = f.getPreferredWidth();
			final int height = f.getPreferredHeight();
			
			if (!_isAnimating) layoutChild(f, width, height);	
			
			setPositionChild(f, 
					af.getCurrentPosition().x, 
					af.getCurrentPosition().y);
		}
		
		public int getPreferredHeight() {
			return Display.getHeight();
		}

		public int getPreferredWidth() {
			return Display.getWidth();
		}
		
		public void onNewFrame(int time) {
			_hello.updatePosition(time, _duration);
			_world.updatePosition(time, _duration);
			
			synchronized(Application.getEventLock()) {
				// tell the framework to update the field layout
				updateLayout();
				
				_isAnimating = time == _duration; 
	 		}
		}
	}
	
	private final TestManager _manager = new TestManager();

	public TestScreen() {
		add(_manager);		
	}

	/**
	 * Start the animation once the screen is displayed
	 */
	protected void onUiEngineAttached(boolean attached) {
		super.onUiEngineAttached(attached);
		
		if (attached) {
			_manager.startAnimation();
		}
	}
}
