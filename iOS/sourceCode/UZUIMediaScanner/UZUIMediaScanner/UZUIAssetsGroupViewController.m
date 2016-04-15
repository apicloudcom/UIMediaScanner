/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */

#import "UZUIAssetsGroupViewController.h"
#import "UZUIAssetsViewController.h"

@interface UZUIAssetsGroupViewController ()
<UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, strong) UITableView *tableView;

@end

@implementation UZUIAssetsGroupViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.title = @"相薄";
    UIButton *cancelBtn = [[UIButton alloc]initWithFrame:CGRectMake(0, 0, 50, 30)];
    cancelBtn.backgroundColor = [UIColor clearColor];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"取消" style:UIBarButtonItemStylePlain target:self action:@selector(cancelBack)];
    if (!self.assetsLibrary) {
        self.assetsLibrary = [self.class defaultAssetsLibrary];
    }
    [self setupGroup];
    [self setUpTableView];
}

- (void)cancelBack {
    [self dismissViewControllerAnimated:YES completion:^(void) {
        self.delegate = nil;
    }];
}

#pragma mark -
#pragma mark  初始化表视图
#pragma mark -

- (void)setUpTableView {
    //创建table view
    self.tableView = [[UITableView alloc] initWithFrame:self.view.bounds];
    self.tableView.backgroundColor = [UIColor whiteColor];
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    
    //使用自适应宽、高，防止tableView横屏后显示不全
    self.tableView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    //加载table view
    [self.view addSubview:self.tableView];
    //分割线
    self.tableView.separatorColor = [UIColor colorWithWhite:0.5 alpha:0.2];
}

- (NSMutableArray *)groups {
    if (!_groups) {
        _groups = [NSMutableArray array];
    }
    return _groups;
}

#pragma mark -
#pragma mark  读取相册列表（组）数据
#pragma mark -

- (void)setupGroup {
    NSMutableArray *assetGroups = nil;
    if (!assetGroups) {
        assetGroups = [[NSMutableArray alloc] init];
    } else {
        [assetGroups removeAllObjects];
    }
    ALAssetsLibraryGroupsEnumerationResultsBlock resultsBlock = ^(ALAssetsGroup *group, BOOL *stop) {
        if (group) {
            if (group.numberOfAssets > 0) {
                [assetGroups addObject:group];
            }
        } else {
            stop = (BOOL *)YES;
        }
        self.groups = [assetGroups mutableCopy];
        [self.tableView reloadData];
    };
    ALAssetsLibraryAccessFailureBlock failureBlock = ^(NSError *error) {
        NSLog(@"Turbo_UIMediascanner_no_right");
    };
    [self.assetsLibrary enumerateGroupsWithTypes:ALAssetsGroupAll
                                      usingBlock:resultsBlock
                                    failureBlock:failureBlock];
}

+ (ALAssetsLibrary *)defaultAssetsLibrary {
    static dispatch_once_t pred = 0;
    static ALAssetsLibrary *library = nil;
    dispatch_once(&pred, ^{
        library = [[ALAssetsLibrary alloc] init];
    });
    return library;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark -
#pragma mark tableView delegate
#pragma mark -

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.groups.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellId = @"AssetsGroup";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellId];
    if (!cell) {
        cell = [[UITableViewCell alloc]initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:cellId];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        cell.backgroundColor = [UIColor whiteColor];
        cell.textLabel.textColor = [UIColor blackColor];
        cell.imageView.contentMode = UIViewContentModeScaleAspectFit;
        //设置cell 右侧箭头图像
        NSString *imgPath = [[NSBundle mainBundle]pathForResource:@"res_UIMediaScanner/item_arrow_indicator" ofType:@"png"];
        UIImage *image = [UIImage imageWithContentsOfFile:imgPath];
        cell.accessoryView = [[UIImageView alloc]initWithImage:image];
    }
    NSString *assetGroupName = [self.groups[indexPath.row] valueForProperty:ALAssetsGroupPropertyName];
    NSInteger assetNum = [self.groups[indexPath.row] numberOfAssets];
    cell.textLabel.text = [NSString stringWithFormat:@"%@ (%ld)",assetGroupName,(long)assetNum ];
    //使用posterImage属性获取相册封面图片
    cell.imageView.image = [UIImage imageWithCGImage:[self.groups[indexPath.row] posterImage]];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 80;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    UZUIAssetsViewController *assetVC = [[UZUIAssetsViewController alloc] initWithDict:self.paramsDict];
    assetVC.delegate = self.delegate;
    //将图片分类显示
    assetVC.isClassify = YES;
    NSInteger selectedIndex = [[self.tableView indexPathForSelectedRow] row];
    assetVC.assetsGroup = self.groups[selectedIndex];  
    [self.navigationController pushViewController:assetVC animated:YES];
}

@end
