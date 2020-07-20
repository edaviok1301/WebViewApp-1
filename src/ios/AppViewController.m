#import "AppViewController.h"

@interface AppViewController (){
    UIActivityIndicatorView *loadingIndicator;
}

@end

@implementation AppViewController

+ (instancetype)sharedHelper{
    
    static AppViewController *sharedClass = nil;
    
    static dispatch_once_t onceToken;
    
    dispatch_once(&onceToken, ^{
        sharedClass = [[self alloc] init];
    });
    
    return sharedClass;
}

- (void)showWebView{
    WKWebViewConfiguration *theConfiguration = [[WKWebViewConfiguration alloc] init];
    _webView = [[WKWebView alloc] initWithFrame:CGRectMake(0, 64, self.view.frame.size.width, self.view.frame.size.height - 44) configuration:theConfiguration];
    _webView.navigationDelegate = self;

    NSMutableURLRequest* request=[NSMutableURLRequest requestWithURL:[NSURL URLWithString:[self.url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]]];
    
    for (NSString * key in [self.headers allKeys]) {
        NSString * value = [self.headers objectForKey:key];
        [request addValue: value forHTTPHeaderField:key];
        [request setValue: value forHTTPHeaderField:key];
    }

    [request setHTTPMethod:@"GET"];
    
    _webView.contentMode = UIViewContentModeScaleAspectFit;
    _webView.allowsBackForwardNavigationGestures = YES;
    
    loadingIndicator = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(0, 200, 200, 28)];
    if (@available(iOS 13.0, *)) {
        [loadingIndicator setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleMedium];
    }
    [loadingIndicator setHidesWhenStopped:YES];
    loadingIndicator.center = _webView.center;
    [_webView addSubview:loadingIndicator];
    
    _webViewController = [[UIViewController alloc] init];
    
    UIToolbar *toolbar = [[UIToolbar alloc] init];
    toolbar.frame = CGRectMake(0, 20, self.view.frame.size.width, 44);
    UIBarButtonItem *contact = [[UIBarButtonItem alloc] initWithTitle:@"Cerrar" style:UIBarButtonItemStyleDone target:self action:@selector(closeButton)];
    NSMutableArray *items = [[NSMutableArray alloc] initWithObjects:contact, nil];
    [toolbar setItems:items animated:YES];
    UIView *newView = [[UIView alloc] initWithFrame:CGRectMake(0,0,self.view.frame.size.width,self.view.frame.size.height)];
    newView.backgroundColor = [UIColor whiteColor];
    
    [newView addSubview:_webView];
    [newView addSubview:toolbar];
    [_webViewController.view addSubview:newView];
    _webViewController.modalPresentationStyle = UIModalPresentationFullScreen;
    UIViewController * uvc = [[[UIApplication sharedApplication] keyWindow] rootViewController];
    [uvc presentViewController:_webViewController animated:YES completion:nil];
    [_webView loadRequest:request];
}

-(void) closeButton{
    [_webViewController dismissViewControllerAnimated:YES completion:nil];
}

- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation{
    [loadingIndicator startAnimating];
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation{
    [loadingIndicator stopAnimating];
}


@end
