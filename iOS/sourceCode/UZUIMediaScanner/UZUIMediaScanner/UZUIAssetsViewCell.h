/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>

@protocol AssetCellViewDelegate;

typedef NSString *(^GetPathBolock)(NSString *);

@interface UZUIAssetsViewCell : UICollectionViewCell

@property (nonatomic, copy) GetPathBolock getPath;
@property (nonatomic, assign) float collectionViewWidth;
@property (nonatomic, strong) NSDictionary *markInfo;
@property (nonatomic, assign) BOOL showPreview, showBrowser;
@property (nonatomic, assign) id <AssetCellViewDelegate> delegate;
@property (nonatomic, strong) UIButton *selectBtn;

- (void)bind:(ALAsset *)asset;

@end

@protocol AssetCellViewDelegate <NSObject>

- (void)didSelectedAssetCell:(UZUIAssetsViewCell *)cell withSelected:(BOOL)select;

@end
