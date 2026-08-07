import { Controller, Get, Post, Body, Patch, Param, Delete, Query, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { ProductsService } from './products.service';
import { CreateProductDto } from './dto/create-product.dto';
import { PaginationQueryDto } from '../../common/dto/pagination.dto';
import { CurrentTenant, TenantContext } from '../../common/decorators/tenant.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { ApiResponseDto } from '../../common/dto/api-response.dto';

@ApiTags('Products')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('products')
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  @Post()
  @ApiOperation({ summary: 'Create new catalog product' })
  async create(@CurrentTenant() tenant: TenantContext, @Body() dto: CreateProductDto) {
    const data = await this.productsService.create(tenant, dto);
    return new ApiResponseDto(data, 'Product created successfully');
  }

  @Get()
  @ApiOperation({ summary: 'Paginated list of products for tenant' })
  async findAll(@CurrentTenant() tenant: TenantContext, @Query() query: PaginationQueryDto) {
    const data = await this.productsService.findAll(tenant, query);
    return new ApiResponseDto(data, 'Products retrieved successfully');
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get product details by ID' })
  async findOne(@CurrentTenant() tenant: TenantContext, @Param('id') id: string) {
    const data = await this.productsService.findOne(tenant, id);
    return new ApiResponseDto(data, 'Product details retrieved successfully');
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update product by ID' })
  async update(
    @CurrentTenant() tenant: TenantContext,
    @Param('id') id: string,
    @Body() dto: Partial<CreateProductDto>,
  ) {
    const data = await this.productsService.update(tenant, id, dto);
    return new ApiResponseDto(data, 'Product updated successfully');
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Soft delete product by ID' })
  async remove(@CurrentTenant() tenant: TenantContext, @Param('id') id: string) {
    await this.productsService.remove(tenant, id);
    return new ApiResponseDto(null, 'Product deleted successfully');
  }
}
