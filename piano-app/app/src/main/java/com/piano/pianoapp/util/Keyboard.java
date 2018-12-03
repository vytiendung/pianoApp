package com.piano.pianoapp.util;

import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;

import android.view.MotionEvent;

public class Keyboard extends CCNode {

	protected CCSprite clickedImage;
	protected CCSprite hintImage;
	protected CCSprite normalImage;
	

	protected ButtonState curState;
	protected boolean isClickEnable;
	protected String note;
	protected int index;
	
	protected CGPoint pos; // pos of sprite
	protected float anchorY;
	protected CGSize size;
	private float scaleX;
	private float scaleY;
	private boolean isBlack;
	private CCSpriteFrameCache spriteFrameCache;
	private CCSpriteFrame spriteFrameWhiteDown;
	private CCSpriteFrame spriteFrameWhiteUp;
	private CCSpriteFrame spriteFrameWhiteHint;
	private CCSpriteFrame spriteFrameBlackDown;
	private CCSpriteFrame spriteFrameBlackHint;
	private CCSpriteFrame spriteFrameBlackUp;

	public void setClickEnable(boolean enable)
	{
		isClickEnable = enable;
	}
	
	public static enum ButtonState {
		NORMAL_STATE,
		CLICKED_STATE,
		HINT_STATE
	}
	
	public Keyboard(CGPoint pos, CGSize size, CCSpriteFrameCache spriteFrameCache, boolean isBlack) {
		setClickEnable(true);
		setRelativeAnchorPoint(true);
		setAnchorPoint(0f, 0f);
		
		this.pos = pos;
		this.spriteFrameCache = spriteFrameCache;
		
		this.isBlack = isBlack;
		if ( isBlack ) {
			this.normalImage = getSpriteBlack();
		} else {
			this.normalImage = getSpriteWhite();
		}
		
		this.normalImage.setRelativeAnchorPoint(true);
		curState = ButtonState.NORMAL_STATE;
		
		setSize(size);
		
		scaleX = (float) size.width / normalImage.getContentSize().width;
		scaleY = (float) size.height / normalImage.getContentSize().height;
		normalImage.setScaleX(scaleX);
		normalImage.setScaleY(scaleY);
		
		this.addChild(normalImage, 0);
		
	}
	
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ButtonState getCurState() {
		return curState;
	}

	public void setCurState(ButtonState curState) {
		this.curState = curState;
	}

	public CGPoint getPos() {
		return pos;
	}


	public void setPos(CGPoint pos) {
		this.pos = pos;
	}

	
	public float getAnchorY() {
		return anchorY;
	}

	public void setAnchorY(float anchorY) {
		this.anchorY = anchorY;
	}

	public CGSize getSize() {
		return size;
	}


	public void setSize(CGSize size) {
		this.size = size;
	}


	public void setDisplayState(ButtonState s)
	{
		if(curState==s)return;
		if(clickedImage!=null) clickedImage.setVisible(false);
		if(hintImage!=null) hintImage.setVisible(false);

		curState = s;
		if(s== ButtonState.CLICKED_STATE && Config.getInstance().enableKeyPressedEffect)
		{

			if(clickedImage==null){
				if(isBlack)
					clickedImage=getClickSpriteBlack();
				else
					clickedImage=getClickSpriteWhite();
			}
			clickedImage.setVisible(true);
			this.addChild(clickedImage,1);

		}
		else if(s== ButtonState.HINT_STATE)
		{

			if(hintImage==null){
				if(isBlack)
					hintImage=getHintSpriteBlack();
				else
					hintImage=getHintSpriteWhite();
			}
			hintImage.setVisible(true);
			this.addChild(hintImage,1);
		}
		
	}
	


	public String getNote() {
		return note;
	}


	public void setNote(String note) {
		this.note = note;
	}
	
	private CCSprite getSpriteBlack(){
		CCSprite sp=null;
		CCSpriteFrame sf;

		if(spriteFrameBlackUp == null) {
			spriteFrameBlackUp = spriteFrameCache.spriteFrameByName("black_up.png");
		}
		if(spriteFrameBlackUp!=null){
			sp=CCSprite.sprite(spriteFrameBlackUp);
			sp.setPosition(pos);
			sp.setAnchorPoint(0.5f,0.5f);
		}

		return sp;
	}
	
	private CCSprite getHintSpriteBlack(){
		CCSprite sp=null;
		CCSpriteFrame sf;

		if(spriteFrameBlackHint == null) {
			spriteFrameBlackHint = spriteFrameCache.spriteFrameByName("black_hint.png");
		}

		if(spriteFrameBlackHint!=null){
			sp=CCSprite.sprite(spriteFrameBlackHint);
			sp.setScaleX(scaleX);
			sp.setScaleY(scaleY);
			sp.setPosition(pos);
			sp.setAnchorPoint(0.5f,0.5f);
		}
		return sp;
		
	}
	
	private CCSprite getClickSpriteBlack(){
		CCSprite sp=null;
		CCSpriteFrame sf;
		if(spriteFrameBlackDown == null) {
			spriteFrameBlackDown = spriteFrameCache.spriteFrameByName("black_down.png");
		}

		if(spriteFrameBlackDown!=null){
			sp=CCSprite.sprite(spriteFrameBlackDown);
			sp.setScaleX(scaleX);
			sp.setScaleY(scaleY);
			sp.setPosition(pos);
			sp.setAnchorPoint(0.5f,0.5f);
		}
		return sp;
		
	}
	
	private CCSprite getHintSpriteWhite(){
		if(spriteFrameWhiteHint == null) {
			spriteFrameWhiteHint = spriteFrameCache.spriteFrameByName("white_hint.png");
		}
		if(spriteFrameWhiteHint==null)return null;
		CCSprite hint = CCSprite.sprite(spriteFrameWhiteHint);
		hint.setScaleX(scaleX);
		hint.setScaleY(scaleY);
		hint.setPosition(pos);
		hint.setAnchorPoint(0.5f,0.5f);
		return hint;
	}
	
	private CCSprite getClickSpriteWhite(){
		if(spriteFrameWhiteDown == null) {
			spriteFrameWhiteDown = spriteFrameCache.spriteFrameByName("white_down.png");
		}
		if(spriteFrameWhiteDown==null)return null;
		CCSprite active = CCSprite.sprite(spriteFrameWhiteDown);
		active.setScaleX(scaleX);
		active.setScaleY(scaleY);
		active.setPosition(pos);
		active.setAnchorPoint(0.5f,0.5f);
		return active;
	}
	
	private CCSprite getSpriteWhite(){
		if(spriteFrameWhiteUp == null) {
			spriteFrameWhiteUp = spriteFrameCache.spriteFrameByName("white_up.png");
		}
		if(spriteFrameWhiteUp==null)return null;
		CCSprite normal = CCSprite.sprite(spriteFrameWhiteUp);
		normal.setPosition(pos);
		normal.setAnchorPoint(0.5f,0.5f);
		return normal;
	}
	
	
	
	@Override
	public boolean equals(Object o) {
		try{
			if(this.getIndex()!=((Keyboard)o).getIndex())
				return false;
			else
				return true;
			
		}catch(Exception e){
			return super.equals(o);
			
		}
				
	}

	public void clearState() {
		if(hintImage!=null){
			hintImage.removeFromParentAndCleanup(true);
			hintImage=null;
		}
		
		if(clickedImage!=null){
			clickedImage.removeFromParentAndCleanup(true);
			hintImage=null;
		}
	}
	
	public  boolean isTouchIn(CGPoint touch){
		CGPoint local = this.convertToNodeSpace(touch);
		CGRect r = this.normalImage.getBoundingBox();
		return CGRect.containsPoint(r, local);

	}
	
	public  boolean isTouchIn(MotionEvent event){
		CGPoint local = this.convertTouchToNodeSpace(event);
		CGRect r = this.normalImage.getBoundingBox();
		return CGRect.containsPoint(r, local);

	}

	public boolean isBlack() {
		return isBlack;
	}

	public void setBlack(boolean isBlack) {
		this.isBlack = isBlack;
	}

	public void setLabel(CCNode label) {
		mlabel = label;
	}

	public CCNode getLabel() {
		return mlabel;
	}

	public void setRect(CCNode rect) {
		mRect = rect;
	}
	public CCNode getRect(){
		return mRect;
	}

	public void setLine(CCNode line) {
		mLine = line;
	}

	public CCNode getLine(){
		return mLine;
	}
	private CCNode mRect;
	private CCNode mlabel;
	private CCNode mLine;
	
	
}
