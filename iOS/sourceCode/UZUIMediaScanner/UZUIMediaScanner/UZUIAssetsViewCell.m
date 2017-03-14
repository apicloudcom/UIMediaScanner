/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import "UZUIAssetsViewCell.h"
#import "NSDictionaryUtils.h"

@interface UZUIAssetsViewCell ()

@property (nonatomic, strong) ALAsset *asset;
@property (nonatomic, strong) UIImage *imageUI;
@property (nonatomic, copy) NSString *type;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, strong) UIImage *videoImage;

@end

@implementation UZUIAssetsViewCell

@synthesize getPath;
@synthesize collectionViewWidth;
@synthesize markInfo;

NSString *(^myBlock)(NSString *path);
UIImage *checkedIcon;

static UIColor *selectedColor;
static UIFont *titleFont;
static CGFloat titleHeight;
static UIImage *videoIcon;
static UIColor *titleColor;

+ (void)initialize {
    titleFont = [UIFont systemFontOfSize:12];
    titleHeight = 20.0f;
    videoIcon = [UIImage imageWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"res_UIMediaScanner/UZAssetsPickerVideo" ofType:@"png"]];
    titleColor = [UIColor whiteColor];
    selectedColor = [UIColor colorWithWhite:1 alpha:0.3];
}

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]){
        self.opaque = YES;
        self.isAccessibilityElement = YES;
        self.accessibilityTraits = UIAccessibilityTraitImage;
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    if (self.showPreview || self.showBrowser) {
        UIImage *selectedImg = nil;
        NSString *icon = [self.markInfo stringValueForKey:@"icon" defaultValue:nil];
        if (icon.length > 0) {
            selectedImg = [UIImage imageWithContentsOfFile:self.getPath(icon)];
        } else {
            selectedImg = [UIImage imageWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"res_UIMediaScanner/UZAssetsPickerChecked" ofType:@"png"]];
        }
        float size = [self.markInfo floatValueForKey:@"size" defaultValue:collectionViewWidth / 3.0];
        self.selectBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _selectBtn.frame = CGRectMake(self.frame.size.width- size, 0, size, size);
        [_selectBtn setImage:selectedImg forState:UIControlStateSelected];
        //[_selectBtn setImageEdgeInsets:UIEdgeInsetsMake(5, 5, 5, 5)];
        UIImage *selectImg = [UIImage imageWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"res_UIMediaScanner/normal" ofType:@"png"]];
        [_selectBtn setImage:selectImg forState:UIControlStateNormal];
        [_selectBtn addTarget:self action:@selector(selectButton:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_selectBtn];
    }
}

- (void)selectButton:(UIButton *)btn {
    [self.selectBtn setSelected:!btn.selected];
    if ([self.delegate respondsToSelector:@selector(didSelectedAssetCell:withSelected:)]) {
        [self.delegate didSelectedAssetCell:self withSelected:self.selectBtn.selected];
    }
}

- (void)bind:(ALAsset *)asset {
    self.asset = asset;
    self.imageUI = [UIImage imageWithCGImage:asset.thumbnail];
    self.type = [asset valueForProperty:ALAssetPropertyType];
    if (self.showPreview || self.showBrowser) {
        [self setNeedsDisplay];
    }
}

- (void)setSelected:(BOOL)selected {
    [super setSelected:selected];
    [self setNeedsDisplay];
}

- (void)drawRect:(CGRect)rect {
    [self.imageUI drawInRect:CGRectMake(0, 0, collectionViewWidth, collectionViewWidth)];
    if (self.showPreview || self.showBrowser) {
        return;
    }
    if ([self.type isEqual:ALAssetTypeVideo]) {
        CGFloat colors [] = {
            0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.8,
            0.0, 0.0, 0.0, 1.0
        };
        CGFloat locations[] = {0.0, 0.75, 1.0};
        CGColorSpaceRef baseSpace = CGColorSpaceCreateDeviceRGB();
        CGGradientRef gradient = CGGradientCreateWithColorComponents(baseSpace, colors, locations, 2);
        
        CGContextRef context = UIGraphicsGetCurrentContext();
        
        CGFloat height = rect.size.height;
        CGPoint startPoint = CGPointMake(CGRectGetMidX(rect), height - titleHeight);
        CGPoint endPoint = CGPointMake(CGRectGetMidX(rect), CGRectGetMaxY(rect));
        CGContextDrawLinearGradient(context, gradient, startPoint, endPoint, kCGGradientDrawsBeforeStartLocation);
        CGSize titleSize = [self.title sizeWithFont:titleFont];
        [titleColor set];
        [self.title drawAtPoint:CGPointMake(rect.size.width - titleSize.width - 2 , startPoint.y + (titleHeight - 12) / 2) forWidth:collectionViewWidth withFont:titleFont fontSize:12 lineBreakMode:NSLineBreakByTruncatingTail baselineAdjustment:UIBaselineAdjustmentAlignCenters];
        [videoIcon drawAtPoint:CGPointMake(2, startPoint.y + (titleHeight - videoIcon.size.height) / 2.0)];
    }
    if (self.showPreview || self.showBrowser) {
        return;
    }
    if (self.selected) {
        CGContextRef context = UIGraphicsGetCurrentContext();
        CGContextSetFillColorWithColor(context, selectedColor.CGColor);
        CGContextFillRect(context, rect);
        //选中的标记的位置
        NSString *position = [self.markInfo stringValueForKey:@"position" defaultValue:@"bottom_left"];
        NSString *icon = [self.markInfo stringValueForKey:@"icon" defaultValue:nil];
        if (icon.length > 0) {
            checkedIcon = [UIImage imageWithContentsOfFile:self.getPath(icon)];
        } else {
            checkedIcon = [UIImage imageWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"res_UIMediaScanner/UZAssetsPickerChecked" ofType:@"png"]];
        }
        float size = [self.markInfo floatValueForKey:@"size" defaultValue:collectionViewWidth / 3.0];
        if ([position isEqualToString:@"bottom_left"]) {
            [checkedIcon drawInRect:CGRectMake(CGRectGetMinX(rect), (CGRectGetMaxY(rect)- size), size, size)];
        } else if ([position isEqualToString:@"top_left"]) {
            [checkedIcon drawInRect:CGRectMake(CGRectGetMinX(rect), CGRectGetMinY(rect), size, size)];
        } else if ([position isEqualToString:@"bottom_right"]) {
            [checkedIcon drawInRect:CGRectMake(CGRectGetMaxX(rect) - size, CGRectGetMaxY(rect) - size, size, size)];
        } else if ([position isEqualToString:@"top_right"]) {
            [checkedIcon drawInRect:CGRectMake(CGRectGetMaxX(rect) - size, CGRectGetMinY(rect), size, size)];
        }
    }
}

@end
