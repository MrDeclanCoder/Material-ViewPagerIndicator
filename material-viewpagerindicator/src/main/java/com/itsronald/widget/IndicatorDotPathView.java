package com.itsronald.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.itsronald.widget.IndicatorDotView.DEFAULT_DOT_COLOR;
import static com.itsronald.widget.IndicatorDotView.DEFAULT_DOT_RADIUS_DIP;
import static com.itsronald.widget.ViewPagerIndicator.DEFAULT_DOT_PADDING_DIP;


/**
 * Reproduces the material animation in which the following occurs:
 *
 * 1. Two dots join to form a path.
 * 2. Starting from one of the original two dot positions, the path shinks toward the other dot.
 * 3. Only one of the original two dots remains.
 *
 * Before starting this view's animation, its two dots should invisibly replace two adjacent dots
 * on the indicator itself.
 */
public class IndicatorDotPathView extends ViewGroup {

    //region Constants

    static final long PATH_STRETCH_ANIM_DURATION = 200; // 200 ms == Android's config_shortAnimTime.
    static final long PATH_RETREAT_ANIM_DURATION = 200; // 200 ms == Android's config_shortAnimTime.

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PATH_DIRECTION_LEFT, PATH_DIRECTION_RIGHT})
    public @interface PathDirection {}

    @PathDirection
    public static final int PATH_DIRECTION_LEFT = 0;
    @PathDirection
    public static final int PATH_DIRECTION_RIGHT = 1;

    //endregion

    @Px
    private int dotPadding;
    @Px
    private int dotRadius;

    //region Subviews

    @NonNull
    private final IndicatorDotView startDot;
    @NonNull
    private final IndicatorDotView endDot;

    /** Portion of the path that stretches out from startDot toward endDot. */
    @NonNull
    private final DotPathSegment startPathSegment;
    /** Portion of the path that stretches out from endDot toward startDot. */
    @NonNull
    private final DotPathSegment endPathSegment;
    /** Portion of the path that grows from where startPathDot and endPathDot meet. */
    @NonNull
    private final ImageView centerSegment;
    @NonNull
    private final ShapeDrawable centerPathShape = new ShapeDrawable(new RectShape());

    //endregion

    //region Constructors

    public IndicatorDotPathView(@NonNull Context context) {
        super(context);

        final float scale = context.getResources().getDisplayMetrics().density;
        this.dotPadding = (int) (DEFAULT_DOT_PADDING_DIP * scale + 0.5);
        this.dotRadius = (int) (DEFAULT_DOT_RADIUS_DIP * scale + 0.5);

        this.startDot = new IndicatorDotView(context);
        this.endDot = new IndicatorDotView(context);
        this.startPathSegment = new DotPathSegment(context);
        this.endPathSegment = new DotPathSegment(context);

        this.centerSegment = new ImageView(context);
        this.centerSegment.setImageDrawable(centerPathShape);

        init(context, DEFAULT_DOT_COLOR);
    }

    public IndicatorDotPathView(@NonNull Context context,
                                @ColorInt int dotColor,
                                @Px int dotPadding,
                                @Px int dotRadius) {
        this(context);

        this.dotPadding = dotPadding;
        this.dotRadius = dotRadius;

        init(context, dotColor);
    }

    public IndicatorDotPathView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        final float scale = context.getResources().getDisplayMetrics().density;
        this.dotPadding = (int) (DEFAULT_DOT_PADDING_DIP * scale + 0.5);
        this.dotRadius = (int) (DEFAULT_DOT_RADIUS_DIP * scale + 0.5);

        this.startDot = new IndicatorDotView(context);
        this.endDot = new IndicatorDotView(context);
        this.startPathSegment = new DotPathSegment(context);
        this.endPathSegment = new DotPathSegment(context);

        this.centerSegment = new ImageView(context);
        this.centerSegment.setImageDrawable(centerPathShape);

        init(context, DEFAULT_DOT_COLOR);
    }

    public IndicatorDotPathView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final float scale = context.getResources().getDisplayMetrics().density;
        this.dotPadding = (int) (DEFAULT_DOT_PADDING_DIP * scale + 0.5);
        this.dotRadius = (int) (DEFAULT_DOT_RADIUS_DIP * scale + 0.5);

        this.startDot = new IndicatorDotView(context);
        this.endDot = new IndicatorDotView(context);
        this.startPathSegment = new DotPathSegment(context);
        this.endPathSegment = new DotPathSegment(context);

        this.centerSegment = new ImageView(context);
        this.centerSegment.setImageDrawable(centerPathShape);

        init(context, DEFAULT_DOT_COLOR);
    }

    //endregion

    private void init(@NonNull Context context,
                      @ColorInt int dotColor) {
        final LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(startDot, -1, layoutParams);
        addView(endDot, -1, layoutParams);
        addView(startPathSegment, -1, layoutParams);
        addView(endPathSegment, -1, layoutParams);
        addView(centerSegment, -1, layoutParams);
        centerSegment.setVisibility(GONE);

        setDotColor(dotColor);
        setDotPadding(dotPadding);
        setDotRadius(dotRadius);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Layout children, starting from the left.
        final int dotDiameter = 2 * dotRadius;
        final int top = getPaddingTop();
        final int bottom = top + dotDiameter;
        int left = getPaddingLeft();

        startDot.layout(left, top, left + dotDiameter, bottom);
        startPathSegment.layout(left, top, left + dotDiameter, bottom);

        left += dotRadius;
        centerSegment.layout(left, top, left + dotPadding + dotDiameter, bottom);

        left += dotRadius + dotPadding;
        endDot.layout(left, top, left + dotDiameter, bottom);
        endPathSegment.layout(left, top, left + dotDiameter, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthPadding = getPaddingLeft() + getPaddingRight();
        final int childWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                widthPadding, LayoutParams.WRAP_CONTENT);
        final int heightPadding = getPaddingTop() + getPaddingBottom();
        final int childHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                heightPadding, LayoutParams.WRAP_CONTENT);

        startDot.measure(childWidthSpec, childHeightSpec);
        startPathSegment.measure(childWidthSpec, childHeightSpec);
        endDot.measure(childWidthSpec, childHeightSpec);
        endPathSegment.measure(childWidthSpec, childHeightSpec);
        centerSegment.measure(childWidthSpec, childHeightSpec);

        // Calculate measurement for this view.
        final int width;
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            final int totalDotWidth = startDot.getMeasuredWidth() + endDot.getMeasuredWidth();
            final int minWidth = ViewCompat.getMinimumWidth(this);
            width = Math.max(minWidth, totalDotWidth + dotPadding + widthPadding);
        }

        final int height;
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            final int indicatorHeight = startDot.getMeasuredHeight();
            final int minHeight = ViewCompat.getMinimumHeight(this);
            height = Math.max(minHeight, indicatorHeight + heightPadding);
        }

        final int childState = ViewCompat.getMeasuredHeightAndState(startDot);
        final int measuredHeight = ViewCompat.resolveSizeAndState(height, heightMeasureSpec,
                childState);
        setMeasuredDimension(width, measuredHeight);
    }

    //region Accessors

    @ColorInt
    int getDotColor() {
        // Relies on the invariant that all subviews are the same color.
        return startDot.getColor();
    }

    void setDotColor(@ColorInt int dotColor) {
        startDot.setColor(dotColor);
        endDot.setColor(dotColor);
        startPathSegment.setColor(dotColor);
        endPathSegment.setColor(dotColor);
        centerPathShape.getPaint().setColor(dotColor);
        invalidate();
    }

    @Px
    int getDotPadding() {
        return dotPadding;
    }

    void setDotPadding(@Px int dotPadding) {
        this.dotPadding = dotPadding;
        centerPathShape.setIntrinsicWidth(dotPadding + 2 * dotRadius);
        invalidate();
        requestLayout();
    }

    @Px
    int getDotRadius() {
        return dotRadius;
    }

    void setDotRadius(@Px int dotRadius) {
        startDot.setRadius(dotRadius);
        endDot.setRadius(dotRadius);
        startPathSegment.setRadius(dotRadius);
        endPathSegment.setRadius(dotRadius);

        final int dotDiameter = 2 * dotRadius;
        centerPathShape.setIntrinsicWidth(dotPadding + dotDiameter);
        centerPathShape.setIntrinsicHeight(dotDiameter);

        invalidate();
        requestLayout();
    }

    //endregion

    public AnimatorSet connectPathAndRetreatAnimator(@PathDirection int pathDirection) {
        final AnimatorSet animatorSet = new AnimatorSet();

        final IndicatorDotView fromDot = pathDirection == PATH_DIRECTION_RIGHT ? startDot : endDot;
        final IndicatorDotView toDot = pathDirection == PATH_DIRECTION_LEFT ? startDot : endDot;
        animatorSet.playSequentially(connectPathSegmentsAnimator(),
                retreatConnectedPathAnimator(fromDot, toDot));

        return animatorSet;
    }

    //region Dot connection animation

    private Animator connectPathSegmentsAnimator() {
        final Rect startSegmentBounds = viewRectInNeighborCoords(startPathSegment, endPathSegment);
        final Rect endSegmentBounds = viewRectInNeighborCoords(endPathSegment, startPathSegment);

        final int startSegmentToX = endSegmentBounds.centerX() < 0 ?
                endSegmentBounds.left : endSegmentBounds.right;
        final int startSegmentToY = endSegmentBounds.centerY() < 0 ?
                endSegmentBounds.top : endSegmentBounds.bottom;
        final int endSegmentToX = startSegmentBounds.centerX() < 0 ?
                startSegmentBounds.left : startSegmentBounds.right;
        final int endSegmentToY = startSegmentBounds.centerY() < 0 ?
                startSegmentBounds.top : startSegmentBounds.bottom;

        final Animator startSegmentAnimator = startPathSegment
                .stretchAnimator(PATH_STRETCH_ANIM_DURATION, startSegmentToX, startSegmentToY);
        final Animator endSegmentAnimator = endPathSegment
                .stretchAnimator(PATH_STRETCH_ANIM_DURATION, endSegmentToX, endSegmentToY);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(startSegmentAnimator, endSegmentAnimator, centerSegmentGrowAnimator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                startDot.setVisibility(VISIBLE);
                endDot.setVisibility(VISIBLE);
            }
        });

        return animatorSet;
    }

    /**
     * Animation: fill out the connecting center dot path to form a straight path between the two
     * dots.
     *
     * @return An animator that grows pathCenter to the appropriate height.
     */
    private Animator centerSegmentGrowAnimator() {
        final float fromScale = 0f, toScale = 1f;

        ObjectAnimator growAnimator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final PropertyValuesHolder scaleYProperty = PropertyValuesHolder
                    .ofFloat(View.SCALE_Y, fromScale, toScale);
            growAnimator = ObjectAnimator.ofPropertyValuesHolder(centerSegment, scaleYProperty);
        } else {
            growAnimator = ObjectAnimator.ofFloat(centerSegment, "scaleY", fromScale, toScale);
        }
        // Start growing when the two ends of the path meet in the middle.
        final long animationDuration = PATH_STRETCH_ANIM_DURATION / 2;
        growAnimator.setStartDelay(animationDuration);
        growAnimator.setDuration(animationDuration);

        growAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                centerSegment.setVisibility(VISIBLE);
            }
        });

        return growAnimator;
    }

    /**
     * Convert the coordinates of one of this view's subviews into the coordinate space of one of
     * this view's other subviews.
     *
     * @param view The view whose bounds to offset into neighbor's coords.
     * @param neighbor The view into whose coordinate space to offset view's bounds.
     * @return The bounds of view in neighbor's coordinate space.
     */
    private Rect viewRectInNeighborCoords(@NonNull View view, @NonNull View neighbor) {
        final Rect bounds = new Rect();
        view.getDrawingRect(bounds);
        offsetDescendantRectToMyCoords(view, bounds);
        offsetRectIntoDescendantCoords(neighbor, bounds);
        return bounds;
    }

    //endregion

    //region Retreat animation

    private Animator retreatConnectedPathAnimator(@NonNull IndicatorDotView fromDot,
                                                  @NonNull IndicatorDotView toDot) {
        Rect endDotBounds = viewRectInNeighborCoords(toDot, fromDot);
        float toX = endDotBounds.left;
        float toY = endDotBounds.top;
        final Animator dotRetreatAnimator =
                retreatDotAnimator(fromDot, toX, toY, PATH_RETREAT_ANIM_DURATION);

        endDotBounds = viewRectInNeighborCoords(toDot, centerSegment);
        toX = endDotBounds.centerX() <= 0 ? 0 : centerSegment.getWidth();
        toY = endDotBounds.centerY() <= 0 ? 0 : centerSegment.getHeight();
        final Animator pathRetreatAnimator =
                retreatCenterSegmentAnimator(toX, toY, PATH_RETREAT_ANIM_DURATION);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(dotRetreatAnimator, pathRetreatAnimator);
        return animatorSet;
    }

    /**
     * Animation: Retreat an indicator dot to a specified position.
     * After the animation, the dot will be invisibly moved back to its original position.
     *
     * @param retreatingDot The dot that should be animated.
     * @param toX The horizontal coordinate to which the dot should move (in its own coordinate space).
     * @param toY The vertical coordinate to which the dot should move (in its own coordinate space).
     * @param animationDuration How long the movement should take, in milliseconds.
     * @return An animator that moves the dot when started.
     */
    private Animator retreatDotAnimator(@NonNull final IndicatorDotView retreatingDot,
                                        final float toX,
                                        final float toY,
                                        final long animationDuration) {
        final Animator dotSlideAnimator = retreatingDot
                .slideAnimator(toX, toY, animationDuration);

        final float originalTranslationX = retreatingDot.getTranslationX();
        final float originalTranslationY = retreatingDot.getTranslationY();
        dotSlideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                retreatingDot.setVisibility(INVISIBLE);
                retreatingDot.setTranslationX(originalTranslationX);
                retreatingDot.setTranslationY(originalTranslationY);
            }
        });

        return dotSlideAnimator;
    }

    private Animator retreatCenterSegmentAnimator(final float toX,
                                               final float toY,
                                               final long animationDuration) {
        final float originalScale = 1, scaleX = 0, scaleY = 1;

        final Animator animator = scaleAnimator(centerSegment, originalScale, scaleX, scaleY);

        // Choose the corner of the view farthest from the destination location.
        final float originalPivotX = getPivotX();
        final float originalPivotY = getPivotY();
        final float pivotX = Math.max(0,  Math.min(toX, centerSegment.getWidth()));
        final float pivotY = Math.max(0, Math.min(toY, centerSegment.getHeight()));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                centerSegment.setPivotX(pivotX);
                centerSegment.setPivotY(pivotY);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Reset values.
                centerSegment.setVisibility(INVISIBLE);
                centerSegment.setScaleX(originalScale);
                centerSegment.setScaleY(originalScale);
                centerSegment.setPivotX(originalPivotX);
                centerSegment.setPivotY(originalPivotY);
            }
        });
        animator.setDuration(animationDuration);

        return animator;
    }


    //endregion

    private static Animator scaleAnimator(final View view,
                                          float originalScale,
                                          float scaleX,
                                          float scaleY) {
        final Animator animator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final PropertyValuesHolder scaleXProperty =
                    PropertyValuesHolder.ofFloat(View.SCALE_X, originalScale, scaleX);
            final PropertyValuesHolder scaleYProperty =
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, originalScale, scaleY);
            animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleXProperty, scaleYProperty);
        } else {
            final Animator scaleXAnimator =
                    ObjectAnimator.ofFloat(view, "scaleX", originalScale, scaleX);
            final Animator scaleYAnimator =
                    ObjectAnimator.ofFloat(view, "scaleY", originalScale, scaleY);

            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
            animator = animatorSet;
        }
        return animator;
    }

    /**
     * An IndicatorDotView that can stretch one of its ends to another location on the screen.
     */
    private static class DotPathSegment extends IndicatorDotView {

        DotPathSegment(@NonNull Context context) {
            super(context);
        }

        //region Path creation

        /**
         * Animation: Stretch one end of this dot toward another location on the screen.
         * When the animation is finished, reset this dot to its original size and position and
         * make it invisible.
         *
         * @param animationDuration How long the animation should take, in milliseconds.
         * @param toX Where to stretch this view horizontally in this view's coordinate space.
         * @param toY Where to stretch this view vertically in this view's coordinate space.
         * @return An animator.
         */
        public Animator stretchAnimator(long animationDuration, final float toX, final float toY) {
            // Since the provided coordinates are in this view's coordinate space, the absolute distance
            // to the coordinate is the value of the coordinate itself.
            final float distanceX = Math.abs(toX) + (toX < 0 ? getWidth() : 0);
            final float distanceY = Math.abs(toY) + (toY < 0 ? getHeight() : 0);

            final float scaleX = distanceX / getWidth();
            final float scaleY = distanceY / getHeight();
            final float originalScale = 1;

            final Animator animator = scaleAnimator(this, originalScale, scaleX, scaleY);

            animator.setDuration(animationDuration);

            final float originalPivotX = getPivotX();
            final float originalPivotY = getPivotY();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setVisibility(VISIBLE);
                    setStretchAnimatorPivot(toX, toY);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // Reset values.
                    setVisibility(INVISIBLE);
                    setScaleX(originalScale);
                    setScaleY(originalScale);
                    setStretchAnimatorPivot(originalPivotX, originalPivotY);
                }
            });

            return animator;
        }

        private void setStretchAnimatorPivot(float toX, float toY) {
            // Ensure pivot is within the view's bounds.
            float pivotX = getWidth() - Math.max(0,  Math.min(toX, getWidth()));
            float pivotY = getHeight() - Math.max(0, Math.min(toY, getHeight()));

            setPivotX(pivotX);
            setPivotY(pivotY);
        }

        //endregion
    }
}
