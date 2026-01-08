export interface AppConfig {
  api: {
    baseUrl: string;
    timeout: number;
  };
  images: {
    placeholders: {
      product: string;
      cart: string;
      profile: string;
    };
    fallbackStrategy: 'local' | 'generated' | 'none';
    s3: {
      bucketUrl?: string;
      region?: string;
    };
  };
  features: {
    enableImageLazyLoading: boolean;
    enableImageOptimization: boolean;
  };
}

export const APP_CONFIG: AppConfig = {
  api: {
    baseUrl: 'http://localhost:8080/api',
    timeout: 30000
  },
  images: {
    placeholders: {
      product: '/assets/images/placeholder-product.svg',
      cart: '/assets/images/placeholder-cart.svg',
      profile: '/assets/images/placeholder-profile.svg'
    },
    fallbackStrategy: 'generated', // 'local' | 'generated' | 'none'
    s3: {
      bucketUrl: 'https://ecommerce-product-images-test.s3.us-east-1.amazonaws.com',
      region: 'us-east-1'
    }
  },
  features: {
    enableImageLazyLoading: true,
    enableImageOptimization: true
  }
};