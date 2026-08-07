import { Controller, Get, Post, Body, Param, Query, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { InvoicesService } from './invoices.service';
import { CreateInvoiceDto } from './dto/create-invoice.dto';
import { PaginationQueryDto } from '../../common/dto/pagination.dto';
import { CurrentTenant, TenantContext } from '../../common/decorators/tenant.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { ApiResponseDto } from '../../common/dto/api-response.dto';

@ApiTags('Invoices')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('invoices')
export class InvoicesController {
  constructor(private readonly invoicesService: InvoicesService) {}

  @Post()
  @ApiOperation({ summary: 'Create invoice' })
  async create(@CurrentTenant() tenant: TenantContext, @Body() dto: CreateInvoiceDto) {
    const data = await this.invoicesService.create(tenant, dto);
    return new ApiResponseDto(data, 'Invoice created successfully');
  }

  @Get()
  @ApiOperation({ summary: 'List invoices' })
  async findAll(@CurrentTenant() tenant: TenantContext, @Query() query: PaginationQueryDto) {
    const data = await this.invoicesService.findAll(tenant, query);
    return new ApiResponseDto(data, 'Invoices retrieved successfully');
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get invoice by ID' })
  async findOne(@CurrentTenant() tenant: TenantContext, @Param('id') id: string) {
    const data = await this.invoicesService.findOne(tenant, id);
    return new ApiResponseDto(data, 'Invoice details retrieved successfully');
  }
}
