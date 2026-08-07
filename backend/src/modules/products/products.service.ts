import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { CreateProductDto } from './dto/create-product.dto';
import { PaginationQueryDto } from '../../common/dto/pagination.dto';
import { TenantContext } from '../../common/decorators/tenant.decorator';

@Injectable()
export class ProductsService {
  constructor(private readonly prisma: PrismaService) {}

  async create(tenant: TenantContext, dto: CreateProductDto) {
    return this.prisma.product.create({
      data: {
        businessId: tenant.businessId,
        categoryId: dto.categoryId,
        name: dto.name,
        sku: dto.sku,
        unit: dto.unit,
        costPrice: dto.costPrice,
        sellingPrice: dto.sellingPrice,
        stockQty: dto.stockQty,
        minStockAlert: dto.minStockAlert,
      },
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
        { name: { contains: search, mode: 'insensitive' } },
        { sku: { contains: search, mode: 'insensitive' } },
      ];
    }

    const [items, total] = await Promise.all([
      this.prisma.product.findMany({
        where,
        skip,
        take: limit,
        orderBy: { [sortBy]: sortOrder },
        include: { category: true },
      }),
      this.prisma.product.count({ where }),
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
    const product = await this.prisma.product.findFirst({
      where: { id, businessId: tenant.businessId, deletedAt: null },
      include: { category: true },
    });

    if (!product) {
      throw new NotFoundException(`Product with ID ${id} not found.`);
    }

    return product;
  }

  async update(tenant: TenantContext, id: string, dto: Partial<CreateProductDto>) {
    await this.findOne(tenant, id);

    return this.prisma.product.update({
      where: { id },
      data: dto,
    });
  }

  async remove(tenant: TenantContext, id: string) {
    await this.findOne(tenant, id);

    return this.prisma.product.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }
}
