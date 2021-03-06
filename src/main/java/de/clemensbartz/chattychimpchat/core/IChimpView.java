/*
 * Copyright (C) 2011 The Android Open Source Project
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

package de.clemensbartz.chattychimpchat.core;

import de.clemensbartz.chattychimpchat.ChimpManager;

import java.io.IOException;
import java.util.List;

/**
 * An interface for view introspection.
 */
public interface IChimpView {

    /**
    * A representation of accessibility ids containing
    * the window id of the accessibility node, and the
    * id of the node itself.
    */
    public static class AccessibilityIds {
        private final int windowId;
        private final long nodeId;

        public AccessibilityIds() {
            this.windowId = 0;
            this.nodeId = 0;
        }

        public AccessibilityIds(int windowId,
                                long nodeId) {
            this.windowId = windowId;
            this.nodeId = nodeId;
        }

        public int getWindowId() {
            return windowId;
        }

        public long getNodeId() {
            return nodeId;
        }
    }

    /**
     * Set the manager for this view to communicate through.
     */
    void setManager(ChimpManager manager);

    /**
     * Obtain the class of the view as a string
     */
    String getViewClass() throws IOException;

    /**
     * Obtain the text contained in the view
     */
    String getText() throws IOException;

    /**
     * Obtain the location of the view on the device screen
     */
    ChimpRect getLocation() throws IOException;

    /**
     * Obtain the checked status of this view.
     */
    boolean getChecked() throws IOException;

    /**
     * Obtain the enabled status of this view.
     */
    boolean getEnabled() throws IOException;

    /**
     * Obtain the selected status of this view.
     */
    boolean getSelected() throws IOException;

    /**
     * Set the selected status of the this  view
     */
    void setSelected(boolean selected) throws IOException;

    /**
     * Obtain the focused status of this view.
     */
    boolean getFocused() throws IOException;

    /**
     * Set the focused status of this view.
     */
    void setFocused(boolean focused) throws IOException;

    /**
     * Retrieve the parent of this view if it has one.
     */
    IChimpView getParent() throws IOException;

    /**
     * Get the children of this view as a list of IChimpViews.
     */
    List<IChimpView> getChildren() throws IOException;

    /**
     * Get the accessibility ids of this view.
     */
    AccessibilityIds getAccessibilityIds() throws IOException;
}
