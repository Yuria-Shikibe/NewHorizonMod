package newhorizon.expand.logic.components;

import arc.math.Mathf;
import arc.util.Time;
import newhorizon.util.struct.TimeQueue;

/**
 * Base class for cutscene actions.
 * Implements timed execution with pause/resume and skip support.
 */
public abstract class Action implements TimeQueue.Timed {
    /** Current elapsed time */
    public float lifeTimer = 0f;
    /** Maximum duration in ticks */
    public float duration = 0f;
    /** Whether this action is paused */
    protected boolean paused = false;
    /** Whether this action has been skipped */
    protected boolean skipped = false;

    public Action(float duration) {
        this.duration = Math.max(0f, duration);
    }

    @Override
    public void update() {
        if (paused || skipped) return;

        if (lifeTimer < duration) {
            lifeTimer += Time.delta;
            act();
        }
    }

    @Override
    public boolean complete() {
        return skipped || lifeTimer >= duration;
    }

    @Override
    public void begin() {
        lifeTimer = 0f;
        paused = false;
        skipped = false;
    }

    @Override
    public void end() {
        // Override for cleanup logic
    }

    /**
     * Called every frame during execution.
     * Override this to implement action behavior.
     */
    public void act() {
    }

    @Override
    public void skip() {
        skipped = true;
        onSkip();
    }

    /**
     * Called when the action is skipped.
     * Override to handle skip behavior.
     */
    protected void onSkip() {
    }

    /**
     * Get the execution progress (0.0 to 1.0).
     * Returns 0.0 if maxTimer is 0 or negative.
     */
    public float progress() {
        if (duration <= 0f) return 0f;
        return Mathf.clamp(lifeTimer / duration, 0f, 1f);
    }

    /**
     * Get the remaining time.
     */
    public float remainingTime() {
        return Math.max(0f, duration - lifeTimer);
    }

    /**
     * Check if the action has been skipped.
     */
    public boolean isSkipped() {
        return skipped;
    }
}
