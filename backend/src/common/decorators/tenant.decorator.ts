import { createParamDecorator, ExecutionContext } from '@nestjs/common';

export interface TenantContext {
  businessId: string;
  branchId?: string;
  userId: string;
  role: string;
}

export const CurrentTenant = createParamDecorator(
  (data: keyof TenantContext | undefined, ctx: ExecutionContext) => {
    const request = ctx.switchToHttp().getRequest();
    const user = request.user;

    if (!user) {
      return null;
    }

    const tenantContext: TenantContext = {
      businessId: user.businessId,
      branchId: user.branchId,
      userId: user.sub || user.userId,
      role: user.role,
    };

    return data ? tenantContext[data] : tenantContext;
  },
);
