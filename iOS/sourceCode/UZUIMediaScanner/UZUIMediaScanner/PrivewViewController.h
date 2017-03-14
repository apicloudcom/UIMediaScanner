//
//  PrivewViewController.h
//  UZApp
//
//  Created by Turbo Sun on 17/2/13.
//  Copyright © 2017年 APICloud. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef void(^completeBlock)(void);

@interface PrivewViewController : UIViewController

@property (nonatomic, assign) NSInteger index;
@property (nonatomic, strong) NSMutableArray *allSelectedAry;
@property (nonatomic, strong) completeBlock comBlock;

@end
