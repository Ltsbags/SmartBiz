import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { CreateInvoiceDto } from './dto/create-invoice.dto';
import { PaginationQueryDto } from '../../common/dto/pagination.dto';
import { TenantContext } from '../../common/decorators/tenant.decorator';

@Injectable()
export class InvoicesService {
  constructor(private readonly prisma: PrismaService) {}

  async create(tenant: TenantContext, dto: CreateInvoiceDto) {
    const branchId = tenant.branchId || (await this.getPrimaryBranchId(tenant.businessId));

    return this.prisma.invoice.create({
      data: {
        businessId: tenant.businessId,
        branchId,
        customerId: dto.customerId,
        invoiceNumber: dto.invoiceNumber,
        status: dto.status,
        totalAmount: dto.totalAmount,
        taxAmount: dto.taxAmount || 0,
        discountAmount: dto.discountAmount || 0,
        paidAmount: dto.paidAmount || 0,
      },
      include: { customer: true, branch: true },
    });
  }

  async findAll(tenant: TenantContext, query: PaginationQueryDto) {
    const { page = 1, limit = 20, search, sortBy = 'createdAt', sortOrder = 'desc' } = query;
    const skip = (page - 1) * limit;

    const where: any = {
      businessId: tenant.businessId,
      deletedAt: null,
    };

    if (search) {
      where.OR = [
        { invoiceNumber: { contains: search, mode: 'insensitive' } },
        { customer: { name: { contains: search, mode: 'insensitive' } } },
      ];
    }

    const [items, total] = await Promise.all([
      this.prisma.invoice.findMany({
        where,
        skip,
        take: limit,
        orderBy: { [sortBy]: sortOrder },
        include: { customer: true, branch: true },
      }),
      this.prisma.invoice.count({ where }),
    ]);

    return {
      items,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit),
      },
    };
  }

  async findOne(tenant: TenantContext, id: string) {
    const invoice = await this.prisma.invoice.findFirst({
      where: { id, businessId: tenant.businessId, deletedAt: null },
      include: { customer: true, branch: true },
    });

    if (!invoice) {
      throw new NotFoundException(`Invoice with ID ${id} not found.`);
    }

    return invoice;
  }

  private async getPrimaryBranchId(businessId: string): Promise<string> {
    const branch = await this.prisma.branch.findFirst({
      where: { businessId, deletedAt: null },
    });
    return branch ? branch.id : '';
  }
}
